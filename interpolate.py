#!/bin/env python2

from commons import *

import numpy as np
import cv2
from cv2 import cv
import random
from scipy import interpolate
import sys
import collections
from time import sleep

class RingBuffer(object):
    
    def __init__(self, max_size):
        self.deque = collections.deque(maxlen = max_size)

    def enqueue(self, item):
        self.deque.append(item)

    def dequeue(self):
        return self.deque.get()

    def __repr__(self):
        return self.deque.__repr__()

    def to_list(self):
        return list(self.deque)

def move_randomly(position, size):
    position = Coordinate(position.x + random.randint(-1, +2), position.y + random.randint(-1, +2))
    size = Size(abs(size.width + random.randint(-1, +2)), abs(size.height + random.randint(-1, +2)))
    return position, size

if __name__ == "__main__":
    position = Coordinate(10, 10)
    size = Size(50, 50)

    MIN_FRAME_ID = 100
    MAX_FRAME_ID = 200

    FOUND_COLOR = Color(0, 0, 255)
    INTERPOLATE_COLOR = Color(0, 0, 127)
 
    #print "%s" % interpolate_coordinates(coordinates)

    #sys.exit(1)
    
    found = False

    RING_BUFFER = RingBuffer(150)
    for frame_id in range(500):
        frame = Frame.blank(frame_id, size = Size(400, 200))
        if MIN_FRAME_ID < frame_id < MAX_FRAME_ID:
            print frame_id
            rectangle = None
            found = False
        else:
            rectangle = Rectangle(position, size)
            if frame_id == MAX_FRAME_ID + 10:
                for i, r in filter(lambda p: p[0] >= MIN_FRAME_ID, enumerate(Rectangle.interpolate(map(lambda p: p[1], filter(lambda p: MIN_FRAME_ID -10 <= p[0] <= MAX_FRAME_ID +10, RING_BUFFER.to_list() + [(frame_id, rectangle)]))), start = MIN_FRAME_ID)):
                    Frame.blank(i, Size(400, 200)).draw_rectangle(r, INTERPOLATE_COLOR).show(wait_delay = 5)
            else:
                frame = frame.draw_rectangle(rectangle, FOUND_COLOR)
            found = True
    
        RING_BUFFER.enqueue((frame_id, rectangle))

        if MIN_FRAME_ID >= frame_id or frame_id > MAX_FRAME_ID + 10:
            frame.show(wait_delay = 5)
        position, size = move_randomly(position, size)



        


