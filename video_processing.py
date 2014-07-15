#!/bin/env python2

import subprocess as sp
from mediainfo import MediaInfo
from time import sleep
import numpy as np
import sys
import cv2
from cv2 import cv
from enum import enum
from ffmpeg import ffmpeg_read, ffmpeg_write, vlc_play, cat, tee
from tracker import Tracker
import threading as t
import multiprocessing as mp
import functools as f
import pickle

def pack(o):
    return pickle.dumps(o)

def unpack(s):
    return pickle.loads(s)


OPENCV_HAAR = "/usr/share/opencv/haarcascades"

Color = enum(RED = (255, 0, 0), GREEN = (0, 0, 255), BLUE = (0, 255, 0), YELLOW = (255, 255, 0))
Haar = enum(FRONTAL_FACE = OPENCV_HAAR + "/haarcascade_frontalface_alt.xml", EYE = OPENCV_HAAR + "/haarcascade_eye.xml", MOUTH = OPENCV_HAAR + "/haarcascade_mcs_mouth.xml", NOSE = OPENCV_HAAR + "/haarcascade_mcs_nose.xml", BODY = OPENCV_HAAR + "/haarcascade_fullbody.xml")

Lbp = enum(FRONTAL_FACE = "/usr/share/opencv/lbpcascades/lbpcascade_frontalface.xml", PROFILE_FACE = "/usr/share/opencv/lbpcascades/lbpcascade_profileface.xml")

VIDEO = "Temoin.mp4"
#VIDEO = "palmashow.flv"

FACE_TRACKER = None
SKIPPED_FRAMES = 2

def draw_boxes(image, boxes, color):
    for x1, y1, x2, y2 in boxes:
        cv2.rectangle(image, (x1, y1), (x2, y2), color, 5)

def normalize_image(image):
    normalized_image = cv2.cvtColor(image, cv.CV_RGB2GRAY)
    normalized_image = cv2.equalizeHist(normalized_image)
    return normalized_image

def haar_cascade(image, xml):
    normalized_image = normalize_image(image)
    #display_image(normalized_image)
    cascade_classifier = cv2.CascadeClassifier(xml)
    boxes = cascade_classifier.detectMultiScale(normalized_image, scaleFactor = 1.3, minNeighbors = 4, minSize = (20, 20), flags = cv.CV_HAAR_SCALE_IMAGE)
    if len(boxes) > 0:
        boxes[:, 2:] += boxes[:, :2]
    else:
        boxes = []
    return boxes

class Frame(object):
    
    def __init__(self, id, array):
        self.id = id
        self.array = array

def read_frames_target(input_queue, output_queue, arguments):
    source, width, height = arguments
    frame_id = 1
    while True:
        frame_bytes = source.read(width * height * 3)

        if frame_bytes == "":
            break
        frame_array= np.fromstring(frame_bytes, dtype = np.uint8).reshape((height, width, 3))
        output_queue.put(Frame(frame_id, frame_array), block = True)
        print "Frame %d read" % frame_id
        frame_id += 1
        
def detect_faces_target(input_queue, output_queue, arguments):
    while True:
        frame = input_queue.get(block = True)
        if frame == "":
            break
        frontal_face_boxes = haar_cascade(frame.array, Haar.FRONTAL_FACE)
        #profile_face_boxes = haar_cascade(frame.array, Lbp.PROFILE_FACE)
        #eye_boxes = []
        #for face_box in face_boxes:
        #    face_frame = crop_frame(frame.array, face_box)
        #    eye_boxes = haar_cascade(face_frame, Haar.EYE)
        #    nose_boxes = haar_cascade(face_frame, Haar.NOSE)
        #    mouth_boxes = haar_cascade(face_frame, Haar.MOUTH)
        #    draw_boxes(frame.array, eye_boxes, Color.GREEN)
        #    draw_boxes(frame.array, nose_boxes, Color.YELLOW)
        #    draw_boxes(frame.array, mouth_boxes, Color.BLUE)

        draw_boxes(frame.array, frontal_face_boxes, Color.RED)
        #draw_boxes(frame.array, profile_face_boxes, Color.GREEN)
        output_queue.put(frame, block = True)

def reorder_frames_target(input_queue, output_queue, arguments):
    frame_buffer = []
    expected_frame_id = 1
    while True:
        print "len(frame_buffer) = %d" % len(frame_buffer)
        frame = input_queue.get(block = True)
        if frame == "":
            break
        frame_buffer.append(frame)
        frame_buffer.sort(key = lambda frame: frame.id)
        while len(frame_buffer) > 0 and frame_buffer[0].id == expected_frame_id:
            frame = frame_buffer.pop(0)
            output_queue.put(frame, block = True)
            expected_frame_id += 1

def display_frames_target(input_queue, output_queue, arguments):
    sink, = arguments
    #if sink is None:
    #    return
    print("sink = %s " % sink)
    while True:
        frame = input_queue.get(block = True)
        if frame == "":
            break
        #cv2.imshow("Frame", frame.array)
        #cv2.waitKey(1)
        sink.write(frame.array.tostring())

def detect(source, sink, media_info):
    read_frames_queue = mp.Queue(maxsize = 100)
    detect_faces_queue = mp.Queue(maxsize = 100)
    reorder_frames_queue = mp.Queue(maxsize = 100)
    
    read_frames_thread = t.Thread(target = read_frames_target, args = [None, read_frames_queue, (source, media_info.width, media_info.height)])
    detect_faces_threads = [mp.Process(target = detect_faces_target, args = [read_frames_queue, detect_faces_queue, ()]) for i in range(3)]
    reorder_frames_thread = t.Thread(target = reorder_frames_target, args = [detect_faces_queue, reorder_frames_queue, ()])
    display_frames_thread = t.Thread(target = display_frames_target, args = [reorder_frames_queue, None, (sink,)])

    read_frames_thread.start()
    for detect_faces_thread in detect_faces_threads:
        detect_faces_thread.start()
    reorder_frames_thread.start()
    display_frames_thread.start()
    #display_frames_target(reorder_frames_queue, None, (sink))
    
def crop_frame(frame, box):
    x1, y1, x2, y2 = box
    mask = np.zeros(frame.shape, dtype=np.uint8)
    roi_corners = np.array([[(x1,y1), (x1,y2), (x2,y2), (x2, y1)]], dtype=np.int32)
    white = (255, 255, 255)
    cv2.fillPoly(mask, roi_corners, white)

    # apply the mask
    masked_image = cv2.bitwise_and(frame, mask)
    return masked_image


def main(video):
    media_info = MediaInfo(video)
    read = ffmpeg_read(video, media_info)
    #print read.stderr.read()
    ##print read.stdout.read()
    ##print read.stderr.read()
    write = ffmpeg_write(media_info)
    print write.stdin
    #sleep(60)
    play = vlc_play(write.stdout)
    #play = cat(write.stdout, "GHOST.flv")
    detect(read.stdout, write.stdin, media_info) #write.stdin, media_info)
    play.wait()

    #read_process = ffmpeg_read(video_input, media_info)
    #with read_process.stdout as read_stdout:
    #   write_process = ffmpeg_write(read_stdout, media_info)

if __name__ == "__main__":
    main(VIDEO if len(sys.argv) < 2 else sys.argv[1])
