#!/bin/env python2

import subprocess as sp
import numpy as np
import cv2
from cv2 import cv
from mediainfo import MediaInfo
from glob import glob

CASCADE_CLASSIFIER = "/usr/share/opencv/haarcascades/haarcascade_frontalface_alt.xml"

def main(image):
    mi = MediaInfo(image)
    height = mi.height
    width = mi.width

    opencv_image = opencv(image, width, height)
    #display_image(opencv_image, title = "OpenCV")

    ffmpeg_frame = ffmpeg(image, width, height)
    #display_image(ffmpeg_frame, title = "ffmpeg")    

    if (opencv_image == ffmpeg_frame).all():
        print "It seems that both are the same... "

    boxes = detect_faces(ffmpeg_frame)
    print "boxes = %s" % boxes
    
    draw_boxes(ffmpeg_frame, boxes)

    #display_image(ffmpeg_frame)

def display_image(image, title = "Untitled"):
    cv2.imshow(title, image)
    key = cv2.waitKey(0)
    cv2.destroyAllWindows()

def draw_boxes(image, boxes):
    for x1, y1, x2, y2 in boxes:
        cv2.rectangle(image, (x1, y1), (x2, y2), (0, 0, 255), 5)

def normalize_image(image):
    normalized_image = cv2.cvtColor(image, cv.CV_RGB2GRAY)
    normalized_image = cv2.equalizeHist(normalized_image)
    return normalized_image

def detect_faces(image):
    normalized_image = normalize_image(image)
    #display_image(normalized_image)
    cascade_classifier = cv2.CascadeClassifier(CASCADE_CLASSIFIER)
    boxes = cascade_classifier.detectMultiScale(normalized_image, scaleFactor = 1.3, minNeighbors = 4, minSize = (20, 20), flags = cv.CV_HAAR_SCALE_IMAGE)
    if len(boxes) > 0:
        boxes[:, 2:] += boxes[:, :2]
    else:
        boxes = []
    return boxes

def ffmpeg(image, width, height):
    command = ["ffmpeg", "-i", image, "-f", "image2pipe", "-s", "%dx%d" % (width, height), "-pix_fmt", "bgr24", "-vcodec", "rawvideo", "-"]
    #print "command = %s" % command
    process = sp.Popen(command, stdin = None, stdout = sp.PIPE, stderr = sp.PIPE, bufsize = width * height * 3)
    frames = []
    with process.stdout as stdout:
        while True:
            raw_frame = stdout.read(width * height * 3)
            #print "raw_frame = %s (length = %d)" % (raw_frame, len(raw_frame))
            if raw_frame == "":
                break
            frame = np.fromstring(raw_frame, dtype = "uint8").reshape((height, width, 3))
            frames.append(frame)
    print "There was %d frames processed." % len(frames)
    #for frame in frames:
        #print "frame = %s" % frame

    return frames[0]

def opencv(image, width, height):
    image = cv2.imread(image, flags = cv2.IMREAD_COLOR)
    #print "image = %s" % image
    return image

if __name__ == "__main__":
    for image in glob("*.png"):
        main(image)
