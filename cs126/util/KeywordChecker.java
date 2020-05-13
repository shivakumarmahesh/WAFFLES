package uk.ac.warwick.cs126.util;

import uk.ac.warwick.cs126.interfaces.IKeywordChecker;
import uk.ac.warwick.cs126.structures.MyHashMap;

public class KeywordChecker implements IKeywordChecker {
    private static final String[] keywords = {
            "0",
            "agreeable",
            "air-headed",
            "apocalypse",
            "appetizing",
            "average",
            "awesome",
            "biohazard",
            "bland",
            "bleh",
            "burnt",
            "charming",
            "clueless",
            "cockroach",
            "cold",
            "crap",
            "dancing",
            "dead",
            "decadent",
            "decent",
            "dirty",
            "disgusting",
            "dreadful",
            "droppings",
            "dry",
            "dumpy",
            "excellent",
            "favourite",
            "feel-good",
            "flavourful",
            "frozen",
            "gem",
            "gross",
            "heart",
            "heavenly",
            "horrendous",
            "horrible",
            "incredible",
            "interesting",
            "lame",
            "lousy",
            "mediocre",
            "meh",
            "mess",
            "microwaved",
            "mouth-watering",
            "nightmares",
            "ok",
            "okay",
            "overcooked",
            "overhyped",
            "perfection",
            "polite",
            "prompt",
            "quality",
            "rude",
            "satisfaction",
            "savoury",
            "sewer",
            "singing",
            "slow",
            "so-so",
            "spongy",
            "sticky",
            "sublime",
            "succulent",
            "sucked",
            "surprised",
            "terrible",
            "tingling",
            "tired",
            "toxic",
            "traumatizing",
            "uncomfortable",
            "under-seasoned",
            "undercooked",
            "unique",
            "unprofessional",
            "waste",
            "worst",
            "yuck",
            "yummy"
    };
    private static MyHashMap<String, Integer> keywordsMap = new MyHashMap<>(97);
    static{
        for(int i = 0; i < keywords.length; i++){
            keywordsMap.add(keywords[i], i);
        }
    }
    public KeywordChecker() {
    }

    public boolean isAKeyword(String word) {
        return keywordsMap.contains(word);
    }

    public int getIndexOfKeyword(String word){
        return (keywordsMap.get(word));
    }

    public String getKeyword(int index){
        return keywords[index];
    } 
}