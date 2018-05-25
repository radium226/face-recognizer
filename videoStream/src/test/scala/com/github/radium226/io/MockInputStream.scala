package com.github.radium226.io

import java.io.InputStream

import com.github.radium226.logging.Logging
import squants.experimental.formatter.DefaultFormatter

import scala.language.postfixOps
import squants.information.Information
import squants.information.InformationConversions._
import squants.experimental.unitgroups.information.MetricInformation

import scala.util.Random

object ReadHandler {

  def ignore(): ReadHandler = { (readByteSize: Information) =>  }

}

trait ReadHandler {

  def handleRead(readSize: Information)

}

object MockInputStream {

  def open(size: Information, readHandler: Option[ReadHandler] = None): MockInputStream = {
    new MockInputStream(size, readHandler.getOrElse(ReadHandler.ignore()))
  }

}

class MockInputStream(totalSize: Information, readHandler: ReadHandler) extends InputStream with Logging {

  def readSize: Information = readByteCount.bytes

  private var readByteCount: Long = 0l
  private val random = new Random()
  private val sizeFormatter = new DefaultFormatter(MetricInformation)

  override def read(): Int = {
    var readByte = -1
    if (readSize < totalSize) {
      readByte = random.nextInt(255)
      readByteCount = readByteCount + 1l
      readHandler.handleRead(readSize)
    }

    if (readByteCount.bytes % (totalSize / 10) == 0) {
      info(s"{} have been read", sizeFormatter.inBestUnit(readSize))
    }

    readByte
  }

}
