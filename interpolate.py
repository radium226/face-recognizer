#!/bin/env python2

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

class Size(object):

    def __init__(self, width, height):
        self.width = width
        self.height = height

    def __str__(self):
        return "{width = %d, height = %d}" % (self.width, self.height)

class Coordinate(object):

    def __init__(self, x, y):
        self.x = x
        self.y = y

    def __repr__(self):
        return "{x = %d, y = %d}" % (self.x, self.y)

class Color(object):

    def __init__(self, red, green, blue):
        self.red = red
        self.green = green
        self.blue = blue

    def to_array(self):
        return np.asarray([self.blue, self.green, self.red], dtype = np.uint8)

class Frame(object):

    def __init__(self, id, array):
        self.array = array
        self.id = id
    
    @staticmethod
    def solid(id, size, color):
        shape = (size.height, size.width, 3)
        array = np.zeros(shape = shape, dtype = np.uint8)
        array[::, ::] = color.to_array()
        frame = Frame(id, array)
        return frame

    def draw_rectangle(self, rectangle, color):
        array = self.array.copy()
        cv2.rectangle(array, (rectangle.position.x, rectangle.position.y), (rectangle.position.x + rectangle.size.width, rectangle.position.y + rectangle.size.height), (color.green, color.red, color.blue), thickness = 2)
        return Frame(self.id, array)

    @property
    def size(self):
        width = self.array.shape[0]
        height = self.array.shape[1]
        return Size(width, height)

    @staticmethod
    def blank(id, size):
        return Frame.solid(id, size, Color(255, 255, 255))

    def show(self, wait_delay = None):
        cv2.imshow("Frame", self.array)
        cv2.waitKey(delay = 0 if wait_delay is None else wait_delay)


class Rectangle(object):
    
    def __init__(self, position, size):
        position = Coordinate(*position) if type(position) == tuple else position
        self.position = position
        self.size = Size(*size) if type(size) == tuple else size

    @staticmethod
    def interpolate(rectangles):
        positions = map(lambda p: Coordinate(*p), interpolate_pairs(map(lambda r: (r.position.x, r.position.y) if r is not None else None, rectangles)))
        sizes = map(lambda p: Size(*p), interpolate_pairs(map(lambda r: (r.size.width, r.size.height) if r is not None else None, rectangles)))
        return map(lambda p: Rectangle(*p), zip(positions, sizes))

    def __repr__(self):
        return "{position = %s, size = %s}" % (self.position, self.size)

def interpolate_pairs(pairs):
    x = map(lambda index_pair: index_pair[0], filter(lambda index_pair: index_pair[1] is not None, enumerate(pairs)))
    y = [pairs[i] for i in x]
    return [pair if pair is not None else (int(interpolate.interp1d(x, map(lambda y: y[0], y))(index)), int(interpolate.interp1d(x, map(lambda y: y[1], y))(index))) for index, pair in enumerate(pairs)]

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



        


