package com.hearthsim.card;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonValue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hearthsim.card.minion.Minion;
import com.hearthsim.card.minion.MinionMock;
import com.hearthsim.card.minion.heroes.TestHero;

public class ImplementedCardList
{
    private static final Logger log = LoggerFactory.getLogger(ImplementedCardList.class);
    
	private static boolean cardIsDisabled(String cardName)
	{
		return cardName.equals("Adrenaline Rush");
	}
	
	private static final ImplementedCardList instance;
	private static final Set<Class<?>> badClasses;
	static
	{
		instance = new ImplementedCardList();
		badClasses = Collections.unmodifiableSet(new HashSet<Class<?>>(Arrays.asList(Card.class, CardMock.class, Minion.class, MinionMock.class, TestHero.class)));
	}
	
	public static ImplementedCardList getInstance()
	{
		return ImplementedCardList.instance;
	}
	
    // TODO: existing clients need to filter out collectibles
    public ImplementedCard getCardForClass(Class<?> clazz) {
    	ImplementedCard result = map_.get(clazz);
    	if (result == null)
    	{
    		if (badClasses.contains(clazz))
    			return null;
    		else
    			throw new RuntimeException("unable to find card for class " + clazz);
    	}
    	else
    		return result;
    }
	
    private ArrayList<ImplementedCard> list_;
    private Map<Class<?>, ImplementedCard> map_;
    private Map<String, ImplementedCard> nameMap = new HashMap<>();
	
    private ImplementedCardList() {
    	list_ = new ArrayList<>();
    	map_ = new HashMap<>();
    	
    	JsonArray mainArr;
    	try
		{
			JsonReader reader = Json.createReader(getClass().getResourceAsStream("/implemented_cards.json"));
			mainArr = reader.readArray();
			reader.close();
		} catch (Exception e)
		{
			// This is really bad and is a critical error if this file can't be read.
			throw new RuntimeException(e);
		}
    	
    	ArrayList<ImplementedCard.Builder> cardBuilders = new ArrayList<>();
    	for(JsonValue val : mainArr)
    	{
    		assert val.getValueType() == JsonValue.ValueType.OBJECT;
    		JsonObject cardOb = (JsonObject)val;
    		ImplementedCard.Builder card = ImplementedCardList.parseCardBuilder(cardOb);
    		cardBuilders.add(card);
    	}
    	
    	this.addCards(cardBuilders);
    }
    
    private void addCards(ArrayList<ImplementedCard.Builder> cardBuilders)
	{
    	JsonObject mainOb;
    	try
		{
			JsonReader reader = Json.createReader(getClass().getResourceAsStream("/AllSets.json"));
			mainOb = reader.readObject();
			reader.close();
		} catch (Exception e)
		{
			// This is really bad and is a critical error if this file can't be read.
			throw new RuntimeException(e);
		}
    	
    	Function<String, SetAndPos> cardNameToSet = ImplementedCardList.getNameToSet(mainOb);
    	for(ImplementedCard.Builder builder : cardBuilders)
    	{
    		JsonObject reference;
    		try
    		{
    			SetAndPos setPos = cardNameToSet.apply(builder.cardName);
    			reference = mainOb.getJsonArray(setPos.set).getJsonObject(setPos.pos);
    		}
    		catch (ClassCastException e)
    		{
    			throw e;
    		}
    		ImplementedCard fullCard = ImplementedCardList.cardFromReference(builder, reference);
    		list_.add(fullCard);
    		map_.put(fullCard.cardClass_, fullCard);
    		nameMap.put(fullCard.name_, fullCard);
    	}
	}
    
    private static ImplementedCard cardFromReference(ImplementedCard.Builder builder, JsonObject reference)
    {
    	if (!builder.cardName.equals( reference.getString("name")))
    		throw new IllegalArgumentException(reference.getString("name") + builder.cardName);
    	
    	// These should exist for every card.
    	builder.type = reference.getString("type");
    	
    	// These may or may not exist for every card.
    	builder.collectible = getOrDefault_Boolean(reference, "collectible");
    	builder.charClass = getOrDefault_String(reference, "playerClass");
    	builder.mana = getOrDefault_Int(reference, "cost");
    	builder.attack = getOrDefault_Int(reference, "attack");
    	builder.health = getOrDefault_Int(reference, "health");
    	builder.durability = getOrDefault_Int(reference, "durability");
    	builder.setText(getOrDefault_String(reference, "text"));
    	builder.rarity = getOrDefault_String(reference, "rarity");
    	builder.setMechanics(getOrDefault_Array(reference, "mechanics"));
    	return builder.build();
    }
    
    private static int getOrDefault_Int(JsonObject ob, String name)
    {
    	try
    	{
    		return ob.getInt(name);
    	}
    	catch (Exception e)
    	{
    		return 0;
    	}
    }
    
    private static boolean getOrDefault_Boolean(JsonObject ob, String name)
    {
    	try
    	{
    		return ob.getBoolean(name);
    	}
    	catch (Exception e)
    	{
    		return false;
    	}
    }
    
    private static String getOrDefault_String(JsonObject ob, String name)
    {
    	try
    	{
    		return ob.getString(name);
    	}
    	catch (Exception e)
    	{
    		return null;
    	}
    }
    
    private static List<String> getOrDefault_Array(JsonObject ob, String name)
    {
    	ArrayList<String> result = new ArrayList<>();
    	try
    	{
    		JsonArray arr = ob.getJsonArray(name);
    		for(int i = 0; i < arr.size(); i++)
    		{
    			String effect = arr.getString(i);
    			result.add(effect);
    		}
    	}catch (Exception e)
    	{
    		
    	}
    	return Collections.unmodifiableList(result);
    }
    
    private static class SetAndPos
    {
    	public final String set;
    	public final int pos;
    	
    	public SetAndPos(String set, int pos)
    	{
    		this.set = set;
    		this.pos = pos;
    	}
    }
    
    private static Function<String, SetAndPos> getNameToSet(JsonObject allSetsOb)
    {
    	HashMap<String, SetAndPos> nameToSet = new HashMap<>();
    	for(String hearthSet : allSetsOb.keySet())
    	{
    		ArrayList<JsonValue> cards = new ArrayList<>(allSetsOb.getJsonArray(hearthSet));
    		int pos = 0;
    		for(JsonValue val : cards)
    		{
    			assert val.getValueType() == JsonValue.ValueType.OBJECT;
    			JsonObject card = (JsonObject)val;
    			if (isBadReferenceCard(card))
    			{
    				pos++;
    				continue;
    			}
    			String name = card.getString("name");
    			assert allSetsOb.getJsonArray(hearthSet).getJsonObject(pos) == card;
    			nameToSet.put(name, new SetAndPos(hearthSet, pos++));
    		}
    	}
    	
    	return (cardName) -> nameToSet.get(cardName);
    }
    
    private static boolean isBadReferenceCard(JsonObject val)
    {    	
    	return ImplementedCardList.cardIsDisabled(val.getString("name")) || val.getString("type").equals("Enchantment");
    }

	private static ImplementedCard.Builder parseCardBuilder(JsonObject obj)
    {
    	Class<?> cardClass;
    	String cardName = obj.getString("name");
    	
    	try
		{
			cardClass = Class.forName(obj.getString("class"));
		} catch (ClassNotFoundException e)
		{
			// Could not parse card.
			log.error("Could not parse in " + cardName + ".");
			return null;
		}
    	
    	ImplementedCard.Builder cardBuilder = new ImplementedCard.Builder(cardName, cardClass);
    	return cardBuilder;
    }
	
    public ImplementedCard getCardForName(String name) {
    	return this.nameMap.get(name);
    }
    
    public List<ImplementedCard> getCardList()
    {
    	return Collections.unmodifiableList(this.list_);
    }
}
