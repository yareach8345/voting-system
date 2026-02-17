package com.yareach._2_voting_system.core.extension

import org.slf4j.Logger
import org.slf4j.LoggerFactory

fun Any.logger(): Logger = LoggerFactory.getLogger(this::class.java)