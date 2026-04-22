package de.sasd.mustelalog.client.service;

import de.sasd.mustelalog.client.model.SavedViewDefinition;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for {@link SavedViewService}.
 */
class SavedViewServiceTest
{
    /**
     * Saves and reloads a saved view.
     *
     * @throws Exception if file handling fails
     */
    @Test
    void saveAndLoadRoundTrip() throws Exception
    {
        Path temp = Files.createTempDirectory("saved-views-test").resolve("views.json");
        SavedViewService service = new SavedViewService(temp);
        SavedViewDefinition definition = new SavedViewDefinition();
        definition.setName("Errors only");
        service.save(definition);
        assertEquals(1, service.loadAll().size());
    }
}
