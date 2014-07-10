#!/bin/env python2

class PutInQueue(object):

    def __init__(self, queue):
        self.queue = queue

    def iterate(self, iterator):
        for item in iterator:
            self.queue.put(item, block = True)
        self.queue.put("", block = True)
    


class GetFromQueue(object):

    def __init__(self, queue):
        self.queue = queue

    def __iter__(self):
        return self

    def next(self):
        item = self.queue.get(block = True)
        if item == "":
            raise StopIteration
        else:
            return item

if __name__ == "__main__":
    import multiprocessing as mp
    import time as t
    import termcolor as tc
    
    def print_with_time(message, color):
        now = t.strftime("%M:%S", t.gmtime())
        print tc.colored("[%s] %s" % (now, message), color)
        
    
    def put_in_queue(queue):
        PutInQueue(queue).iterate(generate_letter())

    def generate_letter():
        for letter in ["a", "b", "c", "d"]:
            print_with_time("I just yield letter %s and now I'll sleep 1 second" % letter, "red")
            yield letter
            t.sleep(1)
        
        print_with_time("I have nothing left to yield but I'll sleep 5 seconds", "red")
        t.sleep(5)

    queue = mp.Queue(maxsize = 1)
    put_in_queue_process = mp.Process(target = put_in_queue, args = [queue])
    put_in_queue_process.start()
    
    for letter in GetFromQueue(queue):
        print_with_time("I just received the letter %s and now I'll sleep 3 seconds" % letter, "blue")
        t.sleep(3)
    
    print_with_time("I will wait for the process... ", "blue")
    put_in_queue_process.join()
    print_with_time("...To join! ", "blue")

