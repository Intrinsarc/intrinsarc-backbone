stratum backbone-profile
    parent backbone
    
{
    component type is-primitive implementation-class Type
    {
    }

    component boolean is-primitive implementation-class java.lang.Boolean
    {
    }

    component Color is-primitive implementation-class java.awt.Color
    {
    }

    component int is-primitive implementation-class java.lang.Integer
    {
    }

    component byte is-primitive implementation-class java.lang.Byte
    {
    }

    component short is-primitive implementation-class java.lang.Short
    {
    }

    component char is-primitive implementation-class java.lang.Char
    {
    }

    component long is-primitive implementation-class java.lang.Long
    {
    }

    component float is-primitive implementation-class java.lang.Float
    {
    }

    component double is-primitive implementation-class java.lang.Double
    {
    }

    component String is-primitive implementation-class java.lang.String
    {
    }

    component Date is-primitive implementation-class java.util.Date
    {
    }

    component Time is-primitive implementation-class java.lang.Long
    {
    }

    component DateTime is-primitive implementation-class java.util.Date
    {
    }

    component Interval is-primitive implementation-class java.lang.Long
    {
    }

    component ValueObject is-primitive implementation-class java.lang.Object
    {
    }

    component element is-stereotype
    {
        attributes:
            bean: boolean,
            implementation-class: String;
    }

    component interface is-stereotype
         resembles element
    {
    }

    component component is-stereotype
         resembles element
    {
        attributes:
            cluster: boolean,
            factory: boolean,
            lifecycle-callbacks: boolean,
            navigable: boolean,
            placeholder: boolean,
            protocols: String;
    }

    component primitive-type is-stereotype
         resembles element
    {
    }

    component stratum is-stereotype
    {
        attributes:
            bb-classpath: String,
            bb-composite-package: String,
            bb-java-folder: String,
            bb-java-suppress: boolean,
            bb-run-component: String,
            bb-run-port: String,
            bb-run-stratum: String,
            bb-source-folder: String,
            bb-source-suppress: boolean,
            check-once-if-read-only: boolean,
            destructive: boolean,
            export-info: String,
            export-version: String,
            generation-profile: String,
            preamble: String,
            relaxed: boolean;
    }

    component connector is-stereotype
    {
        attributes:
            directed: boolean;
    }

    component port is-stereotype
    {
        attributes:
            bean-main: boolean,
            bean-no-name: boolean,
            suppress-generation-port: boolean;
    }

    component attribute is-stereotype
    {
        attributes:
            actual-value: String,
            suppress-generation: boolean;
    }

    component slot is-stereotype
    {
        attributes:
            actual-slot-value: String;
    }

    component visual-effect is-stereotype
    {
    }

    component hide is-stereotype
         resembles visual-effect
    {
    }

    component backbone-delta is-stereotype
    {
    }

    component backbone-overriden-slot is-stereotype
    {
        attributes:
            overriddenSlotAlias: boolean,
            overriddenSlotText: String;
    }

    component trace is-stereotype
    {
    }

    component state is-stereotype
         resembles component
    {
    }

}

