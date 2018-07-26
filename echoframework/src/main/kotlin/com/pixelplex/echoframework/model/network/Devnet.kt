package com.pixelplex.echoframework.model.network

import com.pixelplex.echoframework.model.Address

/**
 * Contains information about echo blockchain development network
 *
 * @author Dmitriy Bushuev
 */
class Devnet : Network(Address.DEVNET_PREFIX)