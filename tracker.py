#!/bin/env python2

import numpy as np
import cv2

class Tracker(object):
    
    def __init__(self, frame, selection):
        x0, y0, x1, y1 = selection
        width = abs(x1 - x0)
        height = abs(y1 - y0)
        print "selection = %s" % selection
        self.track_window = (min(x0, x1), min(y0, y1), width, height)
        print self.track_window
        # [203  92 267 156]
        #cv2.rectangle(frame, (203, 92), (203,92), (255, 255, 255), thickness = 5)
        #cv2.rectangle(frame, (267, 156), (267, 156), (255, 255, 255), thickness = 5)
        #cv2.imshow("frame", frame)
        #cv2.waitKey()
        roi = frame[min(y0, y1):min(y0, y1) + width, min(x0, x1):min(x0, x1) + height]
        #cv2.imshow("ROI", roi)
        #cv2.waitKey()
        hsv_roi =  cv2.cvtColor(roi, cv2.COLOR_BGR2HSV)
        mask = cv2.inRange(hsv_roi, np.array((0., 60.,32.)), np.array((180.,255.,255.)))
        roi_hist = cv2.calcHist([hsv_roi],[0],mask,[180],[0,180])
        self.roi_hist = roi_hist
        cv2.normalize(self.roi_hist, self.roi_hist, 0, 255, cv2.NORM_MINMAX)
        
        self.term_crit = ( cv2.TERM_CRITERIA_EPS | cv2.TERM_CRITERIA_COUNT, 10, 1 )



    def show_hist(self):
        bin_count = self.roi_hist.shape[0]
        bin_w = 24
        img = np.zeros((256, bin_count*bin_w, 3), np.uint8)
        for i in xrange(bin_count):
            h = int(self.roi_hist[i])
            cv2.rectangle(img, (i*bin_w+2, 255), ((i+1)*bin_w-2, 255-h), (int(180.0*i/bin_count), 255, 255), -1)
        img = cv2.cvtColor(img, cv2.COLOR_HSV2BGR)
        cv2.imshow('hist', img)

    def track(self, frame):
        self.frame = frame.copy()
        vis = self.frame.copy()

        hsv = cv2.cvtColor(self.frame, cv2.COLOR_BGR2HSV)
        #mask = cv2.inRange(hsv, np.array((0., 60., 32.)), np.array((180., 255., 255.)))
        dst = cv2.calcBackProject([hsv], [0], self.roi_hist, [0, 180], 1)
        #self.show_hist()
        #dst &= mask
        track_box, self.track_window = cv2.CamShift(dst, self.track_window, self.term_crit)

            #cv2.ellipse(vis, track_box, (0, 0, 255), 2)
        return track_box


