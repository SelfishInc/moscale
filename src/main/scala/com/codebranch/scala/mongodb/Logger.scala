package com.codebranch.scala.mongodb

import org.slf4j.LoggerFactory

object Logger {

  private val logger = LoggerFactory.getLogger("mongo")

  def debug (message: => String): Unit =
    logger.debug(message)

  def error (message: => String): Unit =
    logger.error(message)

  def error (message: => String, error: => Throwable): Unit =
    logger.debug(message, error)
}
