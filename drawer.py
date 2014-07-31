#!/bin/env python2

import cv2
from scipy import interpolate
import pickle
from os.path import isfile

from commons import Frame, Size, Image, Coordinate, Rectangle, Color
from detectors import FaceDetector
from interpolate import RingBuffer

drawing = False
positions = []

STEP = 100
POSITIONS_DUMP_FILENAME = "positions.pickle"

class PositionInterpolator(object):
    
    def __init__(self, f_x, f_y):
        self.f_x = f_x
        self.f_y = f_y

    def __call__(self, t):
        return Coordinate(int(self.f_x(t)), int(self.f_y(t)))

def position_interpolator(background):
    global positions
    if not isfile(POSITIONS_DUMP_FILENAME):
        def callback(event, x, y, flags, parameters):
            if event == 1: #cv2.EVENT_RBUTTONDOWN:
                positions.append(Coordinate(x, y))
    
        cv2.namedWindow("Interpolator")
        cv2.setMouseCallback("Interpolator", callback)

        while True: 
            cv2.imshow("Interpolator", background.array)
            if cv2.waitKey() & 0xFF == 27:
                break
        cv2.destroyWindow("Interpolator")
        with open(POSITIONS_DUMP_FILENAME, "w") as positions_dump_file:
            pickle.dump(positions, positions_dump_file) 
    else:
        with open(POSITIONS_DUMP_FILENAME, "r") as positions_dump_file:
            positions = pickle.load(positions_dump_file)
        
    
    t = map(lambda i: i * STEP, range(len(positions)))
    x = map(lambda p: p.x, positions)
    y = map(lambda p: p.y, positions)



    f_x = interpolate.interp1d(t, x, kind = "quadratic")
    f_y = interpolate.interp1d(t, y, kind = "quadratic")
    
    return PositionInterpolator(f_x, f_y)


def danielle_images():
    background = Image.blank(Size(500, 500))
    danielle = Image.open("./tests/Danielle.jpg")
    interpolator = position_interpolator(background)
    for t in range((len(positions) - 1) * STEP):
        if 50 < t < 100:
            image = background
        else:
            position = interpolator(t)
            danielle.position = position
            image = background.draw_image(danielle)

        yield image

def detect_face(image):
    faces = FaceDetector.for_image(image).detect_faces()
    face = None if len(faces) == 0 else faces[0]
    return face

def sequence(start, stop):
    while start <= stop:
        yield start
        start += 1

class FaceInterpolator(object):
    
    def __init__(self, images):
        self.images = images
        self.image_buffer = []
        self.face_buffer = []

    def interpolate_face(self):
        for image in self.images:
            face = detect_face(image)
            yield face, image

def main():
    for face, image in FaceInterpolator(danielle_images()).interpolate_face():
        if face is not None:
            image = image.draw_rectangle(face, Color(255, 0, 0))
        

        image.show(1)
    #background = Image(Frame.blank(1, Size(500, 300)).array)
    #background.show()

    #danielle = Image.open("../tests/Danielle.jpg")
    #faces = FaceDetector.for_image(danielle).detect_faces()
    #face = faces[0]

    #danielle.crop(rectangle = face).show()    

    #dice.position = Coordinate(0, 20)
    #background.draw_image(dice).show()
    #dice.crop(position = Coordinate(10, 10)).show()


if __name__ == "__main__":
    main()
