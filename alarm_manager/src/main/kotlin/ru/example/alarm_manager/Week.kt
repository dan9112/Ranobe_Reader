package ru.example.alarm_manager

import kotlinx.datetime.DayOfWeek
import ru.example.alarm_manager.TriggerableDay.*
import java.time.DayOfWeek.MONDAY

class Week(startDay: DayOfWeek = MONDAY, weeksTriggers: WeekTriggers) : Iterable<TriggerableDay> {
    private var _startDay: TriggerableDay
    val startDay: TriggerableDay
        get() = _startDay

    fun moveStartNext(steps: Int = 1) = repeat(steps) {
        _startDay = startDay.next
    }

    fun moveStartPrevious(steps: Int = 1) = repeat(steps) {
        _startDay = startDay.previous
    }

    fun moveStartToDay(day: TriggerableDay) {
        while (startDay != day) {
            _startDay = startDay.next
        }
    }

    val all: List<TriggerableDay>
        get() {
            var current = startDay
            val result = ArrayList<TriggerableDay>(7)
            result[0] = current
            repeat(6) {
                current = current.next
                result[it + 1] = current
            }
            return result
        }

    init {
        val monday = Monday(trigger = weeksTriggers.monday)
        val tuesday = Tuesday(trigger = weeksTriggers.tuesday)
        val wednesday = Wednesday(trigger = weeksTriggers.wednesday)
        val thursday = Thursday(trigger = weeksTriggers.thursday)
        val friday = Friday(trigger = weeksTriggers.friday)
        val saturday = Saturday(trigger = weeksTriggers.saturday)
        val sunday = Sunday(trigger = weeksTriggers.sunday)
        _startDay = when (startDay) {
            DayOfWeek.FRIDAY -> friday
            MONDAY -> monday
            DayOfWeek.SATURDAY -> saturday
            DayOfWeek.SUNDAY -> sunday
            DayOfWeek.THURSDAY -> thursday
            DayOfWeek.TUESDAY -> tuesday
            DayOfWeek.WEDNESDAY -> wednesday
        }

        with(receiver = monday) {
            next = tuesday
            previous = sunday
        }
        with(receiver = tuesday) {
            next = wednesday
            previous = monday
        }
        with(receiver = wednesday) {
            next = thursday
            previous = tuesday
        }
        with(receiver = thursday) {
            next = friday
            previous = wednesday
        }
        with(receiver = friday) {
            next = saturday
            previous = thursday
        }
        with(receiver = saturday) {
            next = sunday
            previous = friday
        }
        with(receiver = sunday) {
            next = monday
            previous = saturday
        }
    }

    override fun iterator() = object : Iterator<TriggerableDay> {
        private var current = startDay
        private var start = false
        override fun hasNext() = if (current != startDay) {
            true
        } else if (start) {
            false
        } else {
            start = true
            true
        }

        override fun next() = current.apply {
            current = next
        }
    }
}

interface Triggerable {
    val trigger: Boolean
}

sealed interface TriggerableDay : Triggerable {
    val value: DayOfWeek
    val next: TriggerableDay
    val previous: TriggerableDay
    override var trigger: Boolean

    data class Sunday(override var trigger: Boolean = false) : TriggerableDay {
        override val value = DayOfWeek.SUNDAY
        override lateinit var next: Monday
        override lateinit var previous: Saturday
    }

    data class Monday(override var trigger: Boolean = false) : TriggerableDay {
        override val value = DayOfWeek.MONDAY
        override lateinit var next: Tuesday
        override lateinit var previous: Sunday
    }

    data class Tuesday(override var trigger: Boolean = false) : TriggerableDay {
        override val value = DayOfWeek.TUESDAY
        override lateinit var next: Wednesday
        override lateinit var previous: Monday
    }

    data class Wednesday(override var trigger: Boolean = false) : TriggerableDay {
        override val value = DayOfWeek.WEDNESDAY
        override lateinit var next: Thursday
        override lateinit var previous: Tuesday
    }

    data class Thursday(override var trigger: Boolean = false) : TriggerableDay {
        override val value = DayOfWeek.THURSDAY
        override lateinit var next: Friday
        override lateinit var previous: Wednesday
    }

    data class Friday(override var trigger: Boolean = false) : TriggerableDay {
        override val value = DayOfWeek.FRIDAY
        override lateinit var next: Saturday
        override lateinit var previous: Thursday
    }

    data class Saturday(override var trigger: Boolean = false) : TriggerableDay {
        override val value = DayOfWeek.SATURDAY
        override lateinit var next: Sunday
        override lateinit var previous: Friday
    }
}