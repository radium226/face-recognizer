#!/bin/env python2

import numpy as np
import cv2
from cv2 import cv
from scipy import interpolate, misc

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

    def __init__(self, id, image):
        self.id = id
        self.image = image   

    def show(self, wait_delay = 5):
        self.image.show(wait_delay)

    # http://stackoverflow.com/questions/14063070/overlay-a-smaller-image-on-a-larger-image-python-opencv/14102014#14102014
class Image(object):

    def __init__(self, array):
        self.array = array

    @staticmethod
    def open(path):
        array = misc.imread(path)
        array = cv2.cvtColor(array, cv.CV_RGB2BGR)
        return Image(array)
    
    @property
    def size(self):
        return Size(self.array.shape[1], self.array.shape[0])

    def draw_image(self, image):
        x = image.position.x
        y = image.position.y
        width = image.size.width
        height = image.size.height

        array = self.array.copy()
        array[y:y + height, x:x + width] = image.array[max(0, -y):min([image.size.height, self.size.height - y]), 0:min([image.size.width, self.size.width - x])]
        return Image(array) 
    
    def crop(self, rectangle = None, position = None, size = None):
        if rectangle is None:
            if size is None:
                width = self.size.width - position.x
                height = self.size.height - position.y
                size = Size(width, height)
            rectangle = Rectangle(position, size)
            return self.crop(rectangle = rectangle)
        else:
            x = rectangle.position.x
            y = rectangle.position.y
            width = min([rectangle.size.width, self.size.width - x])
            height = min([rectangle.size.height, self.size.height - y])
            cropped_array = self.array[y:y + height, x:x + width]
            return Image(cropped_array)

    @staticmethod
    def blank(size):
        return Image.solid(size, Color(255, 255, 255))

    def show(self, wait_delay = None):
        cv2.imshow("Frame", self.array)
        cv2.waitKey(delay = 0 if wait_delay is None else wait_delay)

    @staticmethod
    def solid(size, color):
        shape = (size.height, size.width, 3)
        array = np.zeros(shape = shape, dtype = np.uint8)
        array[::, ::] = color.to_array()
        image = Image(array)
        return image

    def draw_rectangle(self, rectangle, color):
        array = self.array.copy()
        cv2.rectangle(array, (rectangle.position.x, rectangle.position.y), (rectangle.position.x + rectangle.size.width, rectangle.position.y + rectangle.size.height), (color.green, color.red, color.blue), thickness = 2)
        return Image(array)

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


