package example.ejb;

import javax.ejb.Stateless;

import org.apache.commons.lang3.math.NumberUtils;

/**
 * Session Bean implementation class ExampleModule
 */
@Stateless
public class ExampleModule implements ExampleModuleLocal {

    public ExampleModule() {        
    }

    @Override
    public int max(int... numbers) {
      return NumberUtils.max(numbers);
    }
    
}
