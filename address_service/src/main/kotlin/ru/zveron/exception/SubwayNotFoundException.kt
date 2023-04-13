package ru.zveron.exception

class SubwayNotFoundException(id: Int) : RuntimeException("Subway station with id $id not found")