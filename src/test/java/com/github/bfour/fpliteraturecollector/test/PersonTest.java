package com.github.bfour.fpliteraturecollector.test;

import java.util.LinkedList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.github.bfour.fpjcommons.services.DatalayerException;
import com.github.bfour.fpjcommons.services.ServiceException;
import com.github.bfour.fpjcommons.services.CRUD.DataIterator;
import com.github.bfour.fpliteraturecollector.domain.Person;
import com.github.bfour.fpliteraturecollector.service.PersonService;
import com.github.bfour.fpliteraturecollector.service.ServiceManager;
import com.github.bfour.fpliteraturecollector.service.ServiceManager.ServiceManagerMode;

public class PersonTest {

	private ServiceManager servMan;
	private PersonService persServ;

	@Before
	public void pre() throws ServiceException {
		this.servMan = ServiceManager.getInstance(ServiceManagerMode.TEST);
		this.persServ = servMan.getPersonService();
	}

	@Test
	public void createAndRemovePersonsAndTestDatabaseClean()
			throws ServiceException, DatalayerException {

		List<Person> personList = new LinkedList<Person>();
		personList.add(new Person("Tapio", "Saari"));
		personList.add(new Person("T.", "Saari"));
		personList.add(new Person("", "Saari"));
		personList.add(new Person("Ilmari", "Sinivuokko"));
		personList.add(new Person("Friðrik Reetta", "Wuopio"));
		personList.add(new Person("Áki Brynhildur", "Jokela"));
		personList.add(new Person("Alan", "Turing"));
		personList.add(new Person("藤本", "雄大"));

		for (Person person : personList)
			persServ.create(person);

		// check all created properly
		DataIterator<Person> dbIterator = persServ.get();
		for (Person person : personList) {
			assert (dbIterator.next().equals(person));
		}

		// delete all
		for (Person person : personList)
			persServ.delete(person);
		
		// confirm delete
		assert(persServ.getAll().isEmpty());

	}

	@After
	public void post() throws ServiceException {
		servMan.resetAllData();
	}

}
