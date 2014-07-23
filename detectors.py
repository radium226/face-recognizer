#!/bin/env python2

from enum import enum
import cv2
from cv2 import cv
from commons import Rectangle, Coordinate, Size

OPENCV_HAAR = "/usr/share/opencv/haarcascades"

Cascades = enum(
    FRONTAL_FACE = OPENCV_HAAR + "/haarcascade_frontalface_alt.xml", 
    EYE = OPENCV_HAAR + "/haarcascade_eye.xml", 
    MOUTH = OPENCV_HAAR + "/haarcascade_mcs_mouth.xml", 
    NOSE = OPENCV_HAAR + "/haarcascade_mcs_nose.xml", 
    BODY = OPENCV_HAAR + "/haarcascade_fullbody.xml"
)


class FaceDetector(object):

    def __init__(self, frame):
        self.frame = frame

    @staticmethod
    def for_frame(frame):
        return FaceDetector(frame)
    
    def detect_faces(self, cascade = Cascades.FRONTAL_FACE):
        array = cv2.cvtColor(self.frame.array, cv.CV_RGB2GRAY)
        array = cv2.equalizeHist(array)
        cascade_classifier = cv2.CascadeClassifier(cascade)
        boxes = cascade_classifier.detectMultiScale(
            array, 
            scaleFactor = 1.3, 
            minNeighbors = 4, 
            minSize = (20, 20), 
            flags = cv.CV_HAAR_SCALE_IMAGE
        )
        
        rectangles = map(lambda box: Rectangle(Coordinate(box[0], box[1]), Size(box[2], box[3])), boxes)
        return rectangles


        for box in boxes:
            print "box = %s" % box
        
        print "boxes = %s" % (boxes if len(boxes) > 0 else "")
        if len(boxes) > 0:
            boxes[:, 2:] += boxes[:, :2]
        else:
            boxes = []
        print "boxes = %s" % boxes
        print "------------------"
        return boxes




        
