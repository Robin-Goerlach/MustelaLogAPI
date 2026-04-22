package de.sasd.mustelalog.client.service;

import de.sasd.mustelalog.client.model.LogEventRecord;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for {@link AggregationService}.
 */
class AggregationServiceTest
{
    /**
     * Verifies that severity aggregation groups equal levels together.
     */
    @Test
    void aggregateBySeverityGroupsItems()
    {
        LogEventRecord first = new LogEventRecord();
        first.setSeverityText("ERROR");
        LogEventRecord second = new LogEventRecord();
        second.setSeverityText("ERROR");
        LogEventRecord third = new LogEventRecord();
        third.setSeverityText("WARNING");

        AggregationService service = new AggregationService(new TimeDisplayService());
        assertEquals(2, service.aggregateBySeverity(List.of(first, second, third)).get(0).getCount());
    }
}
