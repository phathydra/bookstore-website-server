package com.tlcn.books.dto;

import java.util.List;
import java.util.Map;

public class AuthorResponseDto {
    private String name;
    private String birthDate;
    private String birthPlace;
    private List<String> occupation;
    private List<String> genre;
    private String biography;
    private String imageUrl;
    private List<Map<String, Object>> notableWorks;
    private List<Map<String, Object>> awards;
    private Map<String, String> externalLinks;

    // --- Getters & Setters ---
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getBirthDate() { return birthDate; }
    public void setBirthDate(String birthDate) { this.birthDate = birthDate; }

    public String getBirthPlace() { return birthPlace; }
    public void setBirthPlace(String birthPlace) { this.birthPlace = birthPlace; }

    public List<String> getOccupation() { return occupation; }
    public void setOccupation(List<String> occupation) { this.occupation = occupation; }

    public List<String> getGenre() { return genre; }
    public void setGenre(List<String> genre) { this.genre = genre; }

    public String getBiography() { return biography; }
    public void setBiography(String biography) { this.biography = biography; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public List<Map<String, Object>> getNotableWorks() { return notableWorks; }
    public void setNotableWorks(List<Map<String, Object>> notableWorks) { this.notableWorks = notableWorks; }

    public List<Map<String, Object>> getAwards() { return awards; }
    public void setAwards(List<Map<String, Object>> awards) { this.awards = awards; }

    public Map<String, String> getExternalLinks() { return externalLinks; }
    public void setExternalLinks(Map<String, String> externalLinks) { this.externalLinks = externalLinks; }
}
