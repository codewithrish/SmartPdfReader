package com.hackdevelopers.smartpdfreader.events;

public class TextChangedEvent {
  public String newText;
  public TextChangedEvent(String newText) {
      this.newText = newText;
  }
}