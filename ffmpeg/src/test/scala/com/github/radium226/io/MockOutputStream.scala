package com.github.radium226.io

import java.io.OutputStream

import com.github.radium226.logging.Logging
import squants.experimental.formatter.DefaultFormatter
import squants.experimental.unitgroups.information.MetricInformation
import squants.information.{Bytes, Information}
import squants.information.InformationConversions._

object MockOutputStream {

  def open(sizeToBeWritten: Information): MockOutputStream = {
    new MockOutputStream(Some(sizeToBeWritten))
  }

  def open(): MockOutputStream = {
    new MockOutputStream(None)
  }

}

class MockOutputStream(val sizeToBeWritten: Option[Information]) extends OutputStream with Logging {

  private val sizeFormatter = new DefaultFormatter(MetricInformation)
  private var writtenByteCount = 0l

  def writtenSize: Information = {
    Bytes(writtenByteCount)
  }

  override def write(byte: Int): Unit = {
    writtenByteCount = writtenByteCount + 1
    sizeToBeWritten match {
      case Some(sizeToBeWritten) =>
        if (writtenSize % (sizeToBeWritten / 10) == 0) {
          info("{} have been written", sizeFormatter.inBestUnit(writtenSize))
        }
      case None =>
        if (writtenSize % 10.megabytes == 0) {
          info("{} have been written", sizeFormatter.inBestUnit(writtenSize))
        }
    }
  }

}
