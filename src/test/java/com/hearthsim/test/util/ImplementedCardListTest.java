package com.hearthsim.test.util;

import static org.junit.Assert.*;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonValue;

import org.junit.Before;
import org.junit.Test;

import com.hearthsim.card.ImplementedCard;
import com.hearthsim.card.ImplementedCardList;

public class ImplementedCardListTest
{
	@Before
	public void setUp() throws Exception
	{
	}

	@Test
	public void hasAllCards()
	{
		JsonReader reader = Json.createReader(getClass().getResourceAsStream("/implemented_cards.json"));
		JsonArray allCards = reader.readArray();
		reader.close();
		
		assertEquals(allCards.size(), ImplementedCardList.getInstance().getCardList().size());
		for(JsonValue val : allCards)
		{
			JsonObject card = (JsonObject)val;
			String expectedName = card.getString("name");
			ImplementedCard actualCard = ImplementedCardList.getInstance().getCardForName(expectedName);
			assertNotNull(expectedName, actualCard);
			assertEquals(expectedName, actualCard.name_);
			boolean containsVal = false;
			for(ImplementedCard arrayCard : ImplementedCardList.getInstance().getCardList())
				if (arrayCard.name_.equals(expectedName))
					containsVal = true;
			assertTrue(expectedName, containsVal);
		}
	}
}
