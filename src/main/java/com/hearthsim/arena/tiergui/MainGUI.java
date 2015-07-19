package com.hearthsim.arena.tiergui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import com.hearthsim.arena.ArenaGenerator;
import com.hearthsim.card.ImplementedCardList;
import com.hearthsim.card.ImplementedCardList.ImplementedCard;
import com.hearthsim.util.DeckFactory;
import com.hearthsim.util.immutable.ImmutableMap;

public class MainGUI 
{
    public final Dimension ScreenSize = Toolkit.getDefaultToolkit().getScreenSize();
    public final int WindowHeight = (int)this.ScreenSize.getHeight() / 2;
    public final int WindowWidth = (int)this.ScreenSize.getWidth() / 2;
    private final JFrame MainFrame = new JFrame();
    private final JTextField inputRank = new JTextField(5);
    private final JLabel currentCard = new JLabel();
    private final JLabel currentHero = new JLabel();
    private final Function<String, ArrayList<ImplementedCard>> getHeroesCards = MainGUI.getHeroMap();
    private final Set<String> allHeroes = MainGUI.getAllHeroes();
    private final JButton computeButton = new JButton("Compute");
    private final ArrayList<HeroAndCard> cardsToProcess = this.findMissingData(ArenaTierReference::hasData);
    private HeroAndCard currentHeroAndCard = null;
    private final JButton backButton = new JButton("Back");
    private final ArrayList<HeroAndCard> processedCards = new ArrayList<HeroAndCard>();
    
    public static void main(String[] args)
    {
        MainGUI mainGUI = new MainGUI();
        while(true)
            MainGUI.safeSleep(20);
    }
    
    public MainGUI()
    {
        this.initializeFrame();
    }
    
    public static void safeSleep(int milliseconds)
    {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
        }
    }
    
    private void initializeFrame()
    {
        this.MainFrame.setLayout(new FlowLayout());
        initializeComponents();
        this.MainFrame.add(this.computeButton);
        this.MainFrame.setSize(this.WindowWidth, this.WindowHeight);
        this.MainFrame.setVisible(true);
        this.MainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);getClass();
        this.moveIterator();
    }
    
    private void initializeComponents()
    {
        this.computeButton.addActionListener(this::computeButtonClicked);
        this.currentCard.setSize(this.getLongestCardName().length(), 12);
        this.currentCard.setText(this.getLongestCardName());
        this.inputRank.setSize(5, 12);
        this.currentHero.setSize(20, 12);
        this.currentHero.setText("none");
        this.inputRank.addActionListener(this::computeButtonClicked);
        this.backButton.addActionListener(this::backButtonClicked);
        this.addComponents(this.currentHero, this.currentCard, this.inputRank, this.computeButton, this.backButton);
    }
    
    private String getLongestCardName()
    {
        return ArenaGenerator.queryPossibleAttributes((card) -> card.name_)
                .parallelStream().max(MainGUI::compareStringLen).get();
    }
    
    public static Function<String, ArrayList<ImplementedCard>> getHeroMap()
    {
        Set<String> allHeroes = MainGUI.getAllHeroes();
        return (hero) -> new ImmutableMap<String, ArrayList<ImplementedCard>>(allHeroes, MainGUI::getCardsForHero).get(hero);
    }
    
    public static ArrayList<ImplementedCard> getCardsForHero(String hero)
    {
        return new DeckFactory.DeckFactoryBuilder().filterByHero("neutral", hero).buildDeckFactory().getAllPossibleCards();
    }
    
    public static Set<String> getAllHeroes()
    {
        Set<String> allHeroes = ArenaGenerator.queryPossibleAttributes((card) -> card.charClass_);
        allHeroes = new HashSet<>(allHeroes);
        allHeroes.remove("neutral");
        return allHeroes;
    }
    
    public static int compareStringLen(String o1, String o2)
    {
        if (o1.length() > o2.length())
            return 1;
        else if (o1.length() < o2.length())
            return -1;
        else
            return 0;
    }
        
    private ArrayList<HeroAndCard> findMissingData(Function<HeroAndCard, Boolean> hasData)
    {
        ArrayList<HeroAndCard> missingCards = new ArrayList<HeroAndCard>();
        for(String hero : this.allHeroes)
        {
            ArrayList<HeroAndCard> allCardsOfHero = ArenaGenerator
                    .convertCollection(this.getHeroesCards.apply(hero), HeroAndCard.getMap(hero));
            allCardsOfHero.removeIf((card) -> ArenaTierReference.hasData(card));
            missingCards.addAll(allCardsOfHero);
        }
        
        return missingCards;
    }
    
    public static class HeroAndCard implements Serializable
    {
        private static final long serialVersionUID = 1L;
        public final String hero;
        public final String cardName;
        
        public HeroAndCard(String hero, String cardName)
        {
            this.hero = hero;
            this.cardName = cardName;
        }
        
        public static Function<ImplementedCard, HeroAndCard> getMap(String hero)
        {
            return (card) -> new HeroAndCard(hero, card.name_);
        }
        
        @Override
        public boolean equals(Object o)
        {
            if (o == null)
                return false;
            if (!(o instanceof HeroAndCard))
                return false;
            HeroAndCard other = (HeroAndCard)o;
            return other.cardName.equals(this.cardName) && other.hero.equals(this.hero);
        }
        
        @Override
        public int hashCode()
        {
            return this.cardName.hashCode() * this.hero.hashCode() - 1;
        }
    }
    
    private void setCard(HeroAndCard card)
    {
        this.currentCard.setText("Card: " + card.cardName + ":" + MainGUI.getRarity(card));
        this.currentHero.setText("Hero: " + card.hero);
        this.inputRank.setText("");
    }
    
    private void computeButtonClicked(ActionEvent event)
    {
        int rank;
        try
        {
            rank = Integer.parseInt(this.inputRank.getText());
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return;
        }
        
        ArenaTierReference.addLocalData(this.currentHeroAndCard, rank);
        this.processedCards.add(this.currentHeroAndCard);
       
        this.moveIterator();
    }
    
    private void moveIterator()
    {
        if(this.cardsToProcess.isEmpty())
        {
            System.out.println("All cards processed");
            System.exit(0);
        }
        
        HeroAndCard next = MainGUI.popArrayList(this.cardsToProcess);
        if (this.currentHeroAndCard == null || !next.hero.equals(this.currentHeroAndCard.hero))
            MainGUI.changedHero(next.hero);
        
        this.currentHeroAndCard = next;
        this.setCard(this.currentHeroAndCard);
    }
    
    private void addComponents(Component ... comps)
    {
        for(Component comp : comps)
        {
            comp.setVisible(true);
            this.MainFrame.add(comp);
        }
    }
    
    private void backButtonClicked(ActionEvent click)
    {
        if (this.processedCards.isEmpty())
            return;
        
        this.currentHeroAndCard = MainGUI.popArrayList(this.processedCards);
        this.setCard(this.currentHeroAndCard);
        
        try
        {
            this.inputRank.setText(ArenaTierReference.getTierRank(this.currentHeroAndCard).toString());
        }
        catch (Exception e)
        {
            e.printStackTrace();
            this.inputRank.setText("");
        }
    }
    
    public static <T> T popArrayList(ArrayList<T> list)
    {
        int lastIndex = list.size() - 1;
        T result = list.get(lastIndex);
        list.remove(lastIndex);
        return result;
    }
    
    public static void changedHero(String newHero)
    {
        JOptionPane.showMessageDialog(null, "Now inputing cards for: " + newHero + ".");
    }
    
    public static String getRarity(HeroAndCard card)
    {
        // This could be a slowdown if optimization is needed.
        return ImplementedCardList.getInstance().getCardForName(card.cardName).rarity_;
    }
}
