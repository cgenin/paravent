package net.christophe.genin.spring.boot.paravent.queue.core.entities;

import com.openpojo.reflection.filters.FilterPackageInfo;
import com.openpojo.validation.Validator;
import com.openpojo.validation.ValidatorBuilder;
import com.openpojo.validation.rule.impl.EqualsAndHashCodeMatchRule;
import com.openpojo.validation.rule.impl.GetterMustExistRule;
import com.openpojo.validation.rule.impl.SetterMustExistRule;
import com.openpojo.validation.test.impl.GetterTester;
import com.openpojo.validation.test.impl.SetterTester;
import org.junit.Test;

public class PojoTest {

    @Test
    public void test() {
        Validator validator = ValidatorBuilder.create()
                // Add Rules to validate structure for POJO_PACKAGE
                // See com.openpojo.validation.rule.impl for more ...
                .with(new GetterMustExistRule())
                .with(new SetterMustExistRule())
                // Add Testers to validate behaviour for POJO_PACKAGE
                // See com.openpojo.validation.test.impl for more ...
                .with(new SetterTester())
                .with(new GetterTester())
                .with(new EqualsAndHashCodeMatchRule())
                .build();

        validator.validate("net.christophe.genin.spring.boot.paravent.queue.core.entities", new FilterPackageInfo());
    }
}
