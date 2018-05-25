package com.github.radium226.logging

import org.slf4j.LoggerFactory

trait Logging {

  val logger = LoggerFactory.getLogger(getClass)

  def info(message: String, arguments: Any*): Unit = {
    logger.info(message, arguments.map(_.asInstanceOf[Object]):_*)
  }

  def debug(message: String, arguments: Any*): Unit = {
    logger.debug(message, arguments.map(_.asInstanceOf[Object]):_*)
  }

}
