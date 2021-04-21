public enum Type {

  CLINIC_STAFF ("health care provider"),
  GOV_OFFICIAL ("government official"),
  VACCINE_PROVIDER ("vaccine provider"),
  CITIZEN ("citizen");

  private final String strType;

  Type (String strType) {
    this.strType = strType;
  }

  public String getStrType() {
    return this.strType;
  }

}
