package seedu.address.model.util;

import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import seedu.address.model.AddressBook;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.person.Email;
import seedu.address.model.person.ModuleRegistry;
import seedu.address.model.person.ModuleRegistry.Module;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;
import seedu.address.model.person.Role;
import seedu.address.model.person.Telegram;

/**
 * Contains utility methods for populating {@code AddressBook} with sample data.
 */
public class SampleDataUtil {
    public static Person[] getSamplePersons() {
        return new Person[] {
            new Person(new Name("Alex Yeoh"), new Phone("87438807"), new Email("alexyeoh@example.com"),
                Role.TA, getModuleSet("CS2103T"), getTelegram("@sourceacademy")),
            new Person(new Name("Bernice Yu"), new Phone("99272758"), new Email("berniceyu@example.com"),
                Role.PROFESSOR, getModuleSet("CS2040S"), Optional.empty()),
            new Person(new Name("Charlotte Oliveiro"), new Phone("93210283"), new Email("charlotte@example.com"),
                Role.TA, getModuleSet("CS2030S"), getTelegram("@sourceacademy1")),
            new Person(new Name("David Li"), new Phone("91031282"), new Email("lidavid@example.com"),
                Role.PROFESSOR, getModuleSet("CS2100"), getTelegram("@sourceacademy2")),
            new Person(new Name("Irfan Ibrahim"), new Phone("92492021"), new Email("irfan@example.com"),
                Role.TA, getModuleSet("CS2106"), getTelegram("@sourceacademy3")),
            new Person(new Name("Roy Balakrishnan"), new Phone("92624417"), new Email("royb@example.com"),
                Role.PROFESSOR, getModuleSet("CS2109S"), getTelegram("@sourceacademy4")),
            new Person(new Name("Fernandez Keith"), new Phone("90900808"), new Email("fernk@example.com"),
                Role.TA, getModuleSet("CS3230"), getTelegram("@sourceacademy5")),
            new Person(new Name("Ishizawa Naoru"), new Phone("83221199"), new Email("naoishi@example.com"),
                Role.PROFESSOR, getModuleSet("CS2101"), getTelegram("@sourceacademy6"))
        };
    }

    public static ReadOnlyAddressBook getSampleAddressBook() {
        AddressBook sampleAb = new AddressBook();
        for (Person samplePerson : getSamplePersons()) {
            sampleAb.addPerson(samplePerson);
        }
        return sampleAb;
    }

    /**
     * Returns a module set containing the list of strings given.
     */
    public static Set<Module> getModuleSet(String... modules) {
        return Arrays.stream(modules)
                .map(ModuleRegistry::getModuleByCode)
                .collect(Collectors.toSet());
    }

    public static Optional<Telegram> getTelegram(String telegram) {
        return Optional.of(new Telegram(telegram));
    }

}
