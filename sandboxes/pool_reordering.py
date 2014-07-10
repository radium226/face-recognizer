#!/bin/env python2

from multiprocessing import Process, Queue
from threading import Thread
from Queue import Empty, Full
import os
from random import randint
from time import sleep

MIN_SLEEP_TIME = 5
MAX_SLEEP_TIME = 10

END = 0

class Stuff(object):
    
    def __init__(self, id, number):
        self.id = id
        self.number = number

    def __str__(self):
        return "id = %d, number = %d" % (self.id, self.number)

def random_sleep():
    sleep_time = randint(MIN_SLEEP_TIME, MAX_SLEEP_TIME)
    sleep(sleep_time)

def random_number():
    return randint(1, 100)

def alter_data_queue(input_queue, function, output_queue):
    while True:
        input_data = input_queue.get(block = True)
        if input_data == END:
            break
        print "[alter_data_queue] I just got %s (pid=%d)" % (input_data, os.getpid())
        output_data = function(input_data)
        output_queue.put(output_data, block = True)
        print "[alter_data_queue] I just put %s (pid=%d)" % (output_data, os.getpid())
    print "end of alter_data_queue"

def alter_data(stuff):
    #sleep(10 if stuff.id != 2 else 60)
    random_sleep()
    return Stuff(stuff.id, stuff.number ** 2)

def generate_data_queue(output_queue):
    for i in range(10):
        data = Stuff(i, random_number())
        output_queue.put(data, block = True)
        print "[generate_data_queue] I just put %s" % data
        #print data
    print "end of generate_data"

def print_data_queue(input_queue):
    buffer = []
    expected_id = 0
    while True:
        data = input_queue.get(block = True)
        if data == END:
            break
        buffer.append(data)
        buffer.sort(key = lambda s: s.id)
        print "len(buffer) = %d" % len(buffer)
        print "buffer[0] = %s " % (str(buffer[0].id) if len(buffer) > 0 else "None")
        while len(buffer) > 0 and buffer[0].id == expected_id:
            data = buffer.pop(0)
            expected_id += 1
            print "--> [print_data_queue] I just got %s" % data
    print "end of print_data_queue"

def main():
    generate_queue = Queue(maxsize = 1)
    altered_queue = Queue(maxsize = 1)
    
    generate_process = Thread(target = generate_data_queue, args = [generate_queue])
    alter_processes = [Process(target = alter_data_queue, args = [generate_queue, alter_data, altered_queue]) for i in range(10)]
    print_process = Process(target = print_data_queue, args = [altered_queue])
    
    generate_process.start()
    for alter_process in alter_processes:
        print "?"
        alter_process.start()
    print_process.start()

    
    generate_process.join()
    for p in alter_processes:
        generate_queue.put(END, block = True)
    for alter_process in alter_processes:
        alter_process.join()
        print "!!"
    altered_queue.put(END)
    print_process.join()
    print "!!!!"


    
if __name__ == "__main__":
    main()

