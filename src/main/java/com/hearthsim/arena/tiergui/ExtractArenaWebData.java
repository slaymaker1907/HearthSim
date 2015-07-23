package com.hearthsim.arena.tiergui;

import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.function.Function;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.hearthsim.arena.ArenaGenerator;
import com.hearthsim.card.ImplementedCardList.ImplementedCard;
import com.hearthsim.util.immutable.ImmutableMap;

public class ExtractArenaWebData 
{
    public static class ArenaWebData implements Serializable
    {
        private static final long serialVersionUID = 1L;
        public final String hero;
        public final Function<ImplementedCard, Integer> getTier;
        
        public ArenaWebData(String hero, Function<ImplementedCard, Integer> getTier)
        {
            this.hero = hero;
            this.getTier = getTier;
        }
    }
    
    private static class TrGroup
    {
        public final Element headerElement;
        private final ArrayList<Element> cardRows;
        private boolean classSealed = false;
        private Set<String> allCardNames = null;
        
        public TrGroup(Element headerElement)
        {
            this.headerElement = headerElement;
            this.cardRows = new ArrayList<>();
        }
        
        public void addElement(Element e)
        {
            synchronized(this.cardRows)
            {
                if (!this.classSealed)
                    this.cardRows.add(e);
                else
                    throw new IllegalArgumentException("This  class is sealed from further modification.");
            }
        }
        
        public void sealClass()
        {
            synchronized(this.cardRows)
            {
                this.classSealed = true;
            }
        }
        
        public int getTier()
        {
            String headerText = this.headerElement.text();
            final String beginningString = "Tier ";
            return Integer.parseInt(headerText.substring(beginningString.length(), beginningString.length() + 1));
        }
        
        public Set<String> getCards()
        {
            if (this.allCardNames == null)
            {
                HashSet<String> result = new HashSet<>();
                for(Element cardGroup : this.cardRows)
                {
                    Elements cards = TrGroup.getCardElements(cardGroup);
                    for(Element card : cards)
                    {
                        result.add(TrGroup.getImpCard(card));
                    }
                }
                this.allCardNames = Collections.unmodifiableSet(result);
            }
           
            return this.allCardNames;
        }
        
        private static Elements getCardElements(Element parent)
        {
            Elements tdElements = parent.getElementsByTag("td");
            return tdElements;
        }
        
        private static String getImpCard(Element cardElement)
        {
            return cardElement.text();
        }
    }
    
    public static String readFile(String path, Charset encoding) 
            throws IOException 
    {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, encoding);
    }
    
    public static String getHero(Document dataSource)
    {
        ArrayList<String> lowerCaseHeroes = ArenaGenerator.convertCollection(ArenaGenerator.Heroes, String::toLowerCase);
        String lowerCaseTitle = dataSource.title().toLowerCase();
        for(String hero : lowerCaseHeroes)
        {
            if (lowerCaseTitle.contains(hero))
                return hero;
        }
        
        throw new IllegalArgumentException("Input page does not contain a hero's name");
    }
    
    public static Elements getTierTables(Document dataSource)
    {
        Elements tables = dataSource.getElementsByClass("arena_spreadsheet_table");
        assert tables.size() == 4;
        return tables;
    }
    
    public static String getRarityFromTable(Element mainTable)
    {
        String tableIdHeader = "arena_spreadsheet_table_";
        String fullId = mainTable.id();
        return fullId.substring(tableIdHeader.length(), fullId.length());
    }
    
    private static Elements extractTableElements(Element table)
    {
        return table.getElementsByTag("tr");
    }
    
    public static List<TrGroup> groupElements(Elements rawTrs)
    {
        ArrayList<TrGroup> result = new ArrayList<>();
        TrGroup currentGroup = new TrGroup(rawTrs.first());
        result.add(currentGroup);
        for(int i = 1; i < rawTrs.size(); i++)
        {
            Element currentTag = rawTrs.get(i);
            if (currentTag.hasAttr("class"))
            {
                // This is a header tag (with general tier info).
                currentGroup.sealClass();
                currentGroup = new TrGroup(currentTag);
                result.add(currentGroup);
            }
            else
            {
                // This is a card group tab.
                currentGroup.addElement(currentTag);
            }
        }
        
        return Collections.unmodifiableList(result);
    }
    
    public static Function<ImplementedCard, Integer> getFunctionFromGroups(List<TrGroup> groups, List<ImplementedCard> allCards)
    {        
        ImmutableMap<ImplementedCard, Integer> mainMap = new ImmutableMap<ImplementedCard, Integer>(allCards, (cardIns) ->
        {
            String cardName = cardIns.name_;
            for(TrGroup group : groups)
                if (group.getCards().contains(cardName))
                    return group.getTier();
            System.err.println("Could not find a tier for: " + cardName);
            return 100;
        });
        
        return (Function<ImplementedCard, Integer> & Serializable) (card) -> mainMap.get(card);
    }
    
    public static <OutputT> Set<OutputT> aggregateSets(Collection<Collection<OutputT>> toCombine)
    {
        HashSet<OutputT> result = new HashSet<>();
        toCombine.forEach((set) -> result.addAll(set));
        return Collections.unmodifiableSet(result);
    }
    
    public static ArenaWebData parseFile(String fileName) throws IOException
    {
        return ExtractArenaWebData.parseString(ExtractArenaWebData.readFile(fileName, Charset.defaultCharset()));
    }
    
    public static ArenaWebData parseString(String html)
    {
        Document doc = Jsoup.parse(html);
        final String hero = ExtractArenaWebData.getHero(doc);
        Elements tables = ExtractArenaWebData.getTierTables(doc);
        Function<String, List<ImplementedCard>> rarityFunction = ArenaGenerator.getRarityFunction(hero);
        HashMap<String, Function<ImplementedCard, Integer>> functionMap = new HashMap<>();
        for(Element table : tables)
        {
            Tuple<String, Function<ImplementedCard, Integer>> functionPart = ExtractArenaWebData.getFunctionFromTable(table, rarityFunction);
            functionMap.put(functionPart.getFirst(), functionPart.getSecond());
        }
        functionMap.put("free", functionMap.get("common"));
        
        return new ArenaWebData(hero, ExtractArenaWebData.formFunction(functionMap));
    }
    
    public static ArenaWebData parseUrl(String url) throws Exception
    {
        return ExtractArenaWebData.parseString(ExtractArenaWebData.getHtmlFromWeb(url));
    }
    
    public static ArenaWebData parseHero(String hero) throws Exception
    {
        hero = ArenaGenerator.unCapitalize(hero);
        final String url = "http://www.icy-veins.com/hearthstone/arena-" + hero + "-tier-lists-blackrock-mountain";
        return ExtractArenaWebData.parseUrl(url);
    }
    
    private static Function<ImplementedCard, Integer> formFunction(HashMap<String, Function<ImplementedCard, Integer>> rarityFunction)
    {
        return (Function<ImplementedCard, Integer> & Serializable) (card) ->
        {
            String rarity = card.rarity_;
            return rarityFunction.get(rarity).apply(card);
        };
    }
    
    private static Tuple<String, Function<ImplementedCard, Integer>> getFunctionFromTable(Element table, Function<String, List<ImplementedCard>> rarityFunction)
    {
        String rarity = ExtractArenaWebData.getRarityFromTable(table);
        Elements allTrElements = ExtractArenaWebData.extractTableElements(table);
        Function<ImplementedCard, Integer> mainFunction
            = ExtractArenaWebData.getFunctionFromGroups(ExtractArenaWebData.groupElements(allTrElements), rarityFunction.apply(rarity));
        return new Tuple<>(rarity, mainFunction);
    }
    
    public static String getHtmlFromWeb(String url) throws Exception
    {
        Scanner sc = new Scanner(new URL(url).openStream(), "UTF-8");
        sc.useDelimiter("\\A");
        String result = sc.next();
        sc.close();
        return result;
    }
}
