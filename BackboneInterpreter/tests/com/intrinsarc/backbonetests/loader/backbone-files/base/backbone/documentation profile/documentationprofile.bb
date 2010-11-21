stratum documentation-profile
    parent backbone
    
    depends-on backbone-profile
{
    component documentation-top is-stereotype
    {
        attributes:
            copyrightYears: String,
            documentName: String,
            email: String,
            numberOfSpacesForPadding: String,
            owner: String,
            pageTitlePrefix: String,
            siteIndex: String;
    }

    component documentation-included is-stereotype
    {
    }

    component documentation-figure is-stereotype
    {
    }

    component documentation-see-also is-stereotype
    {
    }

    component documentation-image-gallery is-stereotype
    {
    }

}

