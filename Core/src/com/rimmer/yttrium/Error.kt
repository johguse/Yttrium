package com.rimmer.yttrium

/*
 * Contains generic error types used within the server.
 * These errors are caught by the router and transformed into http responses.
 */

/** This is mapped to 404 and should be thrown whenever something that was requested doesn't exist. */
open class NotFoundException : Exception("not_found")

/** This is mapped to 403 and should be thrown whenever a session has insufficient permissions for an operation. */
open class UnauthorizedException(text: String = "invalid_token") : Exception(text)

/** This is mapped to 400 and should be thrown whenever a request tries to do something that's impossible in that context. */
open class InvalidStateException(cause: String): Exception(cause)

/** This is mapped to the provided http code and should be thrown for errors that don't fit any other exception. */
open class HttpException(val errorCode: Int, cause: String): Exception(cause)
