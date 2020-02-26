package in.srijanju.androidapp.model;

import androidx.annotation.NonNull;

import java.io.Serializable;

public class SrijanEvent implements Serializable {
  public String poc;
  public String desc;
  public String name;
  public String type;
  public String rules;
  public String rules_url;
  public String reg_link;
  public String poster;
  public String code;
  public int mints;
  public int maxts;

  @NonNull
  @Override
  public String toString() {
	return name + " - " + type + ": ";
  }
}
