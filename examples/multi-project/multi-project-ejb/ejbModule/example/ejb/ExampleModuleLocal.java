package example.ejb;

import javax.ejb.Local;

@Local
public interface ExampleModuleLocal {

  public int max(int ... numbers);

}
