package tech.xavi.soulsync.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class SearchPolicyConfiguration {

    @Id
    private int id;
    @Column
    private String name;
    @Column(nullable = false)
    private String fileFormatsByComa;
    @Column
    private String wordsToRemoveByComa;
    @Column
    private int minimumMp3Bitrate;
    @Column
    private int minimumMinutesPerRetry;
    @Column
    private int maxRetries;

}
