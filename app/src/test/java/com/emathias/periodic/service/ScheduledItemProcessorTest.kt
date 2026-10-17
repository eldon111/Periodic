package com.emathias.periodic.service

import com.emathias.periodic.db.dao.FakeScheduledItemDao
import com.emathias.periodic.db.dao.FakeScheduledItemHistoryDao
import com.emathias.periodic.db.dao.FakeTodoItemDao
import com.emathias.periodic.db.entities.ScheduledItem
import com.emathias.periodic.model.Schedule
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.time.Instant
import java.time.temporal.ChronoUnit
import kotlin.random.Random

class ScheduledItemProcessorTest {

    private val now: Instant = Instant.now().truncatedTo(ChronoUnit.SECONDS)

    // Daily item whose most recent occurrence was one hour ago and next one is ~23 hours away.
    private val start: Instant = now.minus(3, ChronoUnit.DAYS).minus(1, ChronoUnit.HOURS)
    private val mostRecentOccurrence: Instant = now.minus(1, ChronoUnit.HOURS)

    private val dailyItem = ScheduledItem(
        id = Random.nextLong(),
        title = "daily item",
        description = "daily item",
        schedule = Schedule.of(start, "FREQ=DAILY")!!,
    )

    private lateinit var processor: ScheduledItemProcessor

    @Before
    fun before() {
        processor = ScheduledItemProcessor(
            FakeScheduledItemDao,
            FakeScheduledItemHistoryDao,
            FakeTodoItemDao
        )
    }

    @Test
    fun itFindsNextProcessTime() {
        val cutoff = mostRecentOccurrence.minus(2, ChronoUnit.HOURS)
        val result = processor.findNextProcessTime(dailyItem, cutoff, cutoff)

        Assert.assertEquals(mostRecentOccurrence, result)
    }

    @Test
    fun itFindsNextProcessTimeUsingMoreRecentProcessTime() {
        val result = processor.findNextProcessTime(
            dailyItem,
            now.minus(10, ChronoUnit.DAYS),
            mostRecentOccurrence.minus(1, ChronoUnit.DAYS).plusSeconds(1),
        )

        Assert.assertEquals(mostRecentOccurrence, result)
    }

    @Test
    fun itFindsNextProcessTimeUsingMoreRecentSince() {
        val since = mostRecentOccurrence.minus(1, ChronoUnit.DAYS).plusSeconds(1)
        val result = processor.findNextProcessTime(
            dailyItem,
            since,
            now.minus(10, ChronoUnit.DAYS),
        )

        Assert.assertEquals(mostRecentOccurrence, result)
    }

    @Test
    fun itDoesNotFindProcessTimeIfAlreadyProcessedRecently() {
        val result = processor.findNextProcessTime(
            dailyItem,
            now.minus(3, ChronoUnit.DAYS),
            mostRecentOccurrence,
        )

        Assert.assertNull(result)
    }

    @Test
    fun itDoesNotFindProcessTimeIfCutoffIsTooRecent() {
        val result = processor.findNextProcessTime(dailyItem, now, mostRecentOccurrence.minusSeconds(5))

        Assert.assertNull(result)
    }

    @Test
    fun itDoesNotFindProcessTimeAfterUntil() {
        val item = dailyItem.copy(
            schedule = Schedule.of(
                start,
                "FREQ=DAILY;UNTIL=" + java.time.format.DateTimeFormatter
                    .ofPattern("yyyyMMdd'T'HHmmss'Z'")
                    .withZone(java.time.ZoneOffset.UTC)
                    .format(mostRecentOccurrence.minus(1, ChronoUnit.HOURS)),
            )!!
        )
        val result = processor.findNextProcessTime(
            item,
            mostRecentOccurrence.minusSeconds(60),
            mostRecentOccurrence.minusSeconds(60),
        )

        Assert.assertNull(result)
    }
}
