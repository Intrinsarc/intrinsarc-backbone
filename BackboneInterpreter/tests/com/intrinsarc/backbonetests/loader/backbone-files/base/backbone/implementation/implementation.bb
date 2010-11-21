stratum implementation
    parent backbone
    
    depends-on backbone-profile, api
{
    component Creator implementation-class com.intrinsarc.backbone.runtime.implementation.Creator
    {
        attributes:
            factoryNumber: int;
        ports:
            create provides ICreate;
    }

    component FactoryBase is-factory
    {
        ports:
            creator is-create-port provides ICreate;
    }

    component State implementation-class com.intrinsarc.backbone.runtime.implementation.State
    {
        «state» 
        ports:
            in provides ITransition,
            out requires ITransition,
            events provides IEvent;
    }

    component Start is-normal implementation-class com.intrinsarc.backbone.runtime.implementation.Terminal
         resembles State
    {
        «state» 
        delete-ports:
            events;
        ports:
            start-terminal provides ITerminal;
        replace-ports:
            replaced-in becomes replaced-in provides ITransition;
        port-links:
            in-out joins in to out;
    }

    component End is-normal implementation-class com.intrinsarc.backbone.runtime.implementation.Terminal
         resembles State
    {
        «state» 
        delete-ports:
            events;
        ports:
            end-terminal provides ITerminal;
        replace-ports:
            replaced-ina becomes replaced-ina provides ITransition,
            replaced-out becomes replaced-out requires ITransition;
        port-links:
            in-outa joins in to out;
    }

    component CompositeState is-normal
         resembles State
    {
        «state» 
        replace-ports:
            replaced-outa becomes replaced-outa requires ITransition;
        parts:
            start: Start,
            end: End;
    }

    component StateDispatcher implementation-class com.intrinsarc.backbone.runtime.implementation.StateDispatcher
    {
        ports:
            dEvents provides IEvent,
            dDispatch requires IEvent,
            dStart requires ITerminal,
            dEnd requires ITerminal;
        port-links:
            dEvents-dDispatch joins dEvents to dDispatch;
    }

}

