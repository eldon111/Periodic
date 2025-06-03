package com.emathias.periodic.service

import com.emathias.periodic.db.dao.FakeScheduledItemDao
import com.emathias.periodic.db.dao.FakeScheduledItemHistoryDao
import com.emathias.periodic.db.dao.FakeTodoItemDao
import com.emathias.periodic.db.entities.ScheduledItem
import com.emathias.periodic.util.CronUtils
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.time.OffsetDateTime
import kotlin.random.Random

class ScheduledItemProcessorTest {

    val now: OffsetDateTime = OffsetDateTime.now()
    val mostRecentHour: OffsetDateTime = now.withMinute(0).withSecond(0).withNano(0)
    val oneHourAgo: OffsetDateTime = mostRecentHour.minusHours(1)
    val twoHoursAgo: OffsetDateTime = mostRecentHour.minusHours(2)
    val threeHoursAgo: OffsetDateTime = mostRecentHour.minusHours(3)

    val hourlyItem = ScheduledItem(
        id = Random.nextLong(),
        title = "hourly item",
        description = "hourly item",
        startsAt = now.toInstant(),
        repeats = true,
        cronExpression = CronUtils.parseCronExpression("0 * * * *"),
    )

    lateinit var processor: ScheduledItemProcessor

    @Before
    fun before() {
        processor =
            ScheduledItemProcessor(
                FakeScheduledItemDao,
                FakeScheduledItemHistoryDao,
                FakeTodoItemDao
            )
    }

    @Test
    fun itFindsNextProcessTime() {
        val result = processor.findNextProcessTime(
            hourlyItem,
            twoHoursAgo.toInstant(),
            twoHoursAgo.toInstant()
        )

        Assert.assertNotNull(result)
        Assert.assertEquals(oneHourAgo.toInstant(), result)
    }

    @Test
    fun itFindsNextProcessTimeMoreRecent() {
        val result = processor.findNextProcessTime(
            hourlyItem,
            oneHourAgo.toInstant(),
            oneHourAgo.toInstant()
        )

        Assert.assertNotNull(result)
        Assert.assertEquals(mostRecentHour.toInstant(), result)
    }

    @Test
    fun itFindsNextProcessTimeOlder() {
        val result = processor.findNextProcessTime(
            hourlyItem,
            threeHoursAgo.toInstant(),
            twoHoursAgo.toInstant()
        )

        Assert.assertNotNull(result)
        Assert.assertEquals(oneHourAgo.toInstant(), result)
    }

    @Test
    fun itDoesNotFindProcessTimeIfAlreadyProcessedRecently() {
        val result = processor.findNextProcessTime(
            hourlyItem,
            threeHoursAgo.toInstant(),
            mostRecentHour.toInstant()
        )

        Assert.assertNull(result)
    }

    @Test
    fun itDoesNotFindProcessTimeIfCutoffIsTooRecent() {
        val result = processor.findNextProcessTime(
            hourlyItem,
            now.toInstant(),
            twoHoursAgo.toInstant()
        )

        Assert.assertNull(result)
    }
}

