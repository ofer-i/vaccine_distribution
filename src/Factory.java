public class Factory {

  Factory() {

  }

  static IController createController(Type type) {
    if (type.equals(Type.CITIZEN)) {
      return new ControllerCitizen();
    } else if (type.equals(Type.CLINIC_STAFF)) {
      return new ControllerClinic();
    } else if (type.equals(Type.GOV_OFFICIAL)) {
      return new ControllerGov();
    } else if (type.equals(Type.VACCINE_PROVIDER)) {
      return new ControllerDistributor();
    } else {
      throw new IllegalArgumentException("unknown user type");
    }
  }

}

