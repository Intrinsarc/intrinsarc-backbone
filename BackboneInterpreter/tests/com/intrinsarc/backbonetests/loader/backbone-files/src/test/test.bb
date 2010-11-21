stratum 1b6f8bf2-0b26-4256-be5b-b57e1a931911/test/
    parent 8b3c3119-b181-4e0a-84d4-4868a7328a97/model/
     is-relaxed
    depends-on backbone
{
    interface 0c81e34c-aa3e-44ba-8260-737fa5b629d5/IFace/ implementation-class test.IFace
    {
    }

    component f23ffbab-2d2b-4182-b152-96a498018c5b/A/ implementation-class test.A
    {
        attributes:
            fe5f1c72-3ca4-4cc8-a4c4-0b848fd5d749/age/: int;
        ports:
            839a6dfe-3f1d-424a-bd8d-e7e158611fe9/port/ provides IRun;
    }

    component d161022c-ee9c-437a-9e8b-9518a441af5d/B/
    {
        ports:
            f8eb1b0c-459d-4b24-a65d-af98bd20c631/p/;
        parts:
            e7341f4f-e556-4acf-82c7-0a06d75bbe50: f23ffbab-2d2b-4182-b152-96a498018c5b/A/
                fe5f1c72-3ca4-4cc8-a4c4-0b848fd5d749/age/ = 5;
        connectors:
            8673edbf-35db-4e04-aabc-109ed8449860 joins 839a6dfe-3f1d-424a-bd8d-e7e158611fe9/port/@e7341f4f-e556-4acf-82c7-0a06d75bbe50 to f8eb1b0c-459d-4b24-a65d-af98bd20c631/p/;
    }

}

