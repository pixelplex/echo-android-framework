package com.pixelplex.echolib.exception

/**
 * Represents errors associated with illegal Address state
 * (@see com.pixelplex.echolib.model.Address)
 *
 * @author Dmitriy Bushuev
 * @author Darya
 */
class MalformedAddressException : RuntimeException {

    constructor() : super()
    constructor(message: String?) : super(message)
    constructor(message: String?, cause: Throwable?) : super(message, cause)

}
