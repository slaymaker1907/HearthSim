package com.hearthsim.card;

import java.lang.reflect.Constructor;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.hearthsim.card.minion.Hero;

public class ImplementedCard implements Comparable<ImplementedCard>
{
    private static final Pattern htmlTagPattern = Pattern.compile("<[a-zA-Z_0-9\\/]+?", 0);
    private static final Pattern overloadPattern = Pattern.compile("Overload:\\s+?\\((\\d+)\\)");
    private static final Pattern spellEffectPattern = Pattern.compile("\\$(\\d+) damage");
    // TODO This regex assumes spell damage is at the beginning of a line because of Ancient Mage
    private static final Pattern spellDamagePattern = Pattern.compile("^Spell Damage\\s+\\+(\\d+)");
    
    private static String MECHANICS_TAUNT = "Taunt";
    private static String MECHANICS_SHIELD = "Divine Shield";
    private static String MECHANICS_WINDFURY = "Windfury";
    private static String MECHANICS_CHARGE = "Charge";
    private static String MECHANICS_STEALTH = "Stealth";
    private static String MECHANICS_CANT_ATTACK = "Can\'t Attack.";
    
    public final Class<?> cardClass_;
    public final String name_, type_, charClass_, rarity_, text_, race;
    public final int mana_, attack_, health_, durability, overload, spellDamage, spellEffect;
    public final boolean isHero, collectible, taunt_, divineShield_, windfury_, charge_, stealth_, cantAttack;
    
    protected ImplementedCard(ImplementedCard.Builder builder)
    {
    	this.name_ = builder.cardName;
    	this.cardClass_ = builder.cardClass;
    	this.type_ = builder.type;
    	this.charClass_ = builder.charClass;
    	this.rarity_ = builder.rarity;
    	this.text_ = builder.getText();
    	this.race = builder.race;
    	this.mana_ = builder.mana;
    	this.attack_ = builder.attack;
    	this.health_ = builder.health;
    	this.durability = builder.durability;
    	this.isHero = builder.isHero;
    	this.collectible = builder.collectible;
    	this.overload = builder.getOverload();
    	this.spellDamage = builder.getSpellDamage();
    	this.spellEffect = builder.getSpellEffect();
    	this.windfury_ = builder.isWindfury();
    	this.taunt_ = builder.isTaunt();
    	this.stealth_ = builder.isStealth();
    	this.divineShield_ = builder.isDivineShield();
    	this.charge_ = builder.isCharge();
    	this.cantAttack = builder.isCantAttack();
    }

    @Override
    public int compareTo(ImplementedCard o) {
        int result = Integer.compare(this.mana_, o.mana_);
        if (result == 0) {
            result = this.name_.compareTo(o.name_);
        }
        return result;
    }

    public Card createCardInstance() {
		try
		{
	        Constructor<?> ctor = this.cardClass_.getConstructor();
			return (Card)ctor.newInstance();
		} catch (Exception e)
		{
			// This is really bad, generate a runtime exception.
			throw new RuntimeException(e);
		}
    }
	
	@Override
	public boolean equals(Object o)
	{
		try
		{
			ImplementedCard otherCard = (ImplementedCard)o;
			return this.name_.equals(otherCard.name_);
		}
		catch (Exception e)
		{
			return false;
		}
	}
	
	@Override
	public int hashCode()
	{
		return this.name_.hashCode();
	}
	
	public static class Builder
	{
		public final String cardName;
		public final Class<?> cardClass;
		public final boolean isHero;
		
		private String text;
		private int spellDamage, spellEffect, overload;
		private boolean taunt, divineShield, windfury, charge, stealth, cantAttack;

		public int attack, health, durability, mana;
		public boolean collectible;
	    public String type, charClass, rarity, race;
		
		public Builder(String cardName, Class<?> cardClass)
		{
			this.cardName = cardName;
			this.cardClass = cardClass;
			this.isHero = Hero.class.isAssignableFrom(this.cardClass);
			
			this.attack = this.health = this.durability = this.mana = this.spellDamage = this.spellEffect = this.overload = 0;
			this.collectible = this.taunt = this.divineShield = this.windfury = this.charge = this.stealth = this.cantAttack = false;
			this.type = this.charClass = this.rarity = this.text = this.race = null;
		}
		
		public ImplementedCard.Builder setText(String text)
		{
			if (text == null)
				this.text = "";
			else
				this.text = text;
			
			String cleanedText = this.text.equals("") ? "" : ImplementedCard.htmlTagPattern.matcher(this.text).replaceAll("");
			this.cantAttack = this.text.contains(ImplementedCard.MECHANICS_CANT_ATTACK);
			
            if (!cleanedText.equals("")) {
                Matcher matcher = ImplementedCard.overloadPattern.matcher(cleanedText);
                this.overload = Builder.getMatchVal(matcher);

                matcher = ImplementedCard.spellDamagePattern.matcher(cleanedText);
                this.spellDamage = Builder.getMatchVal(matcher);

                matcher = ImplementedCard.spellEffectPattern.matcher(cleanedText);
                this.spellEffect = Builder.getMatchVal(matcher);
            }
			
			return this;
		}
		
		private static int getMatchVal(Matcher matcher)
		{
			try
			{
				return Integer.parseInt(matcher.group(1));
			}
			catch (Exception e)
			{
				return 0;
			}
		}
		
		public ImplementedCard.Builder setMechanics(String mechanics)
		{
			if (mechanics == null)
				mechanics = "";
			
			this.taunt = mechanics.contains(ImplementedCard.MECHANICS_TAUNT);
			this.stealth = mechanics.contains(ImplementedCard.MECHANICS_STEALTH);
			this.divineShield = mechanics.contains(ImplementedCard.MECHANICS_SHIELD);
			this.windfury = mechanics.contains(ImplementedCard.MECHANICS_WINDFURY);
			this.charge = mechanics.contains(ImplementedCard.MECHANICS_CHARGE);
			
			return this;
		}
		
		public String getText()
		{
			return text;
		}
		
		public int getSpellDamage()
		{
			return spellDamage;
		}

		public int getSpellEffect()
		{
			return spellEffect;
		}

		public int getOverload()
		{
			return overload;
		}
		
		public boolean isTaunt()
		{
			return taunt;
		}

		public boolean isDivineShield()
		{
			return divineShield;
		}

		public boolean isWindfury()
		{
			return windfury;
		}

		public boolean isCharge()
		{
			return charge;
		}

		public boolean isStealth()
		{
			return stealth;
		}

		public boolean isCantAttack()
		{
			return cantAttack;
		}
		
		public ImplementedCard build()
		{
			return new ImplementedCard(this);
		}
	}
}