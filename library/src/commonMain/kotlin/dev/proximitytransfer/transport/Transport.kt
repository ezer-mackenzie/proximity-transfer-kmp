package dev.proximitytransfer.transport

interface Transport {
    suspend fun open(): Connection
}
