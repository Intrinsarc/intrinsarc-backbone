stratum 3a7c94bf-a683-4bf4-abbd-72f821eeb197/dual_window/ is-relaxed is-destructive
    depends-on 1a218c04-d44e-4750-8275-b225a44eca4e/LTSA/
{
    component 64aca208-9442-4be9-9292-c5c96819ead3/DualWindow/
    {
        ports:
            «port» cb340c9b-d72d-4128-9dc9-4d2a7a04d2cc/window/ provides b8c1c9e2-1dfd-48b3-8ddf-9de667288d92/IWindow/,
            «port» f06e19f7-0b0b-4471-b9c1-38f1c375b1df/bigFont/,
            «port» 0cd9b6f1-b65f-4856-a87d-9a94ae10a1d4/bigFont/;
        parts:
            bcfe7ddb-1b31-476a-b011-020e9424ad6e: 78f252b9-b031-4463-8bba-4566b4385ee8/WindowCombiner/,
            e601efdd-acad-4a1a-a966-c0538e2abb35: 7cc98f81-8cd4-48b1-a121-880c879c2839/TransitionsWindow/
                2cb307bf-4e0a-448c-a97f-539b66beb744/extension/ = ".aut",
            e3ac3806-45db-4d65-96f9-609213eb9a35: 3a720347-363a-4af4-a3e3-efc92e3cb57d/AlphabetWindow/;
        connectors:
            031487d2-68bb-47a5-8f07-5cce1eb86839 joins cb340c9b-d72d-4128-9dc9-4d2a7a04d2cc/window/ to c575bec6-a2f5-4c71-9b82-1826810fefcf/main/@bcfe7ddb-1b31-476a-b011-020e9424ad6e,
            4e1c7dd1-650e-48f2-995d-a3c939954e02 joins 829c065d-a019-4142-9868-4cff47099acf/window/@e3ac3806-45db-4d65-96f9-609213eb9a35 to a325179e-09b4-4bc3-94e4-6136c8de1820/windows/@bcfe7ddb-1b31-476a-b011-020e9424ad6e,
            90bcbb97-7071-4d21-91fa-0cd095d145ff joins 0bcadf31-fbf2-4981-b72e-8166f88f070f/window/@e601efdd-acad-4a1a-a966-c0538e2abb35 to a325179e-09b4-4bc3-94e4-6136c8de1820/windows/@bcfe7ddb-1b31-476a-b011-020e9424ad6e,
            7a2b715d-5afe-47ef-add2-a8e411d401b1 joins 5f3c0f1e-083a-45ae-8251-f21d33145549/bigFont/@e601efdd-acad-4a1a-a966-c0538e2abb35 to f06e19f7-0b0b-4471-b9c1-38f1c375b1df/bigFont/,
            bc92c64c-d785-47ac-b379-d7e12ba4090f joins 0cd9b6f1-b65f-4856-a87d-9a94ae10a1d4/bigFont/ to ec8dcfa6-dd0a-456b-a573-3603b0a2ddb0/bigFont/@e3ac3806-45db-4d65-96f9-609213eb9a35;
    }

    component 78f252b9-b031-4463-8bba-4566b4385ee8/WindowCombiner/ implementation-class combined.WindowCombiner
    {
        ports:
            «port» c575bec6-a2f5-4c71-9b82-1826810fefcf/main/ provides b8c1c9e2-1dfd-48b3-8ddf-9de667288d92/IWindow/,
            «port» a325179e-09b4-4bc3-94e4-6136c8de1820/windows/ requires b8c1c9e2-1dfd-48b3-8ddf-9de667288d92/IWindow/;
    }

    component 3899efcd-c376-474b-b372-a143e0d076ea/DualWindowFactory/
         resembles FactoryBase
    {
        ports:
            «port» 88e6a2d4-e7e3-4fbb-ac34-af441dc2a65e/bigFont/,
            «port» c8eefb73-a170-4e39-8573-41d4ef779cfb/window/,
            «port» 7a10deea-97ca-4a58-b2c7-22fb523d98d8/bigFont/;
        parts:
            ad5cfc81-a6ab-45f6-9fb6-a84994ccd0fa: 64aca208-9442-4be9-9292-c5c96819ead3/DualWindow/;
        connectors:
            22c88170-a84b-43f2-b529-c849054fcd53 joins f06e19f7-0b0b-4471-b9c1-38f1c375b1df/bigFont/@ad5cfc81-a6ab-45f6-9fb6-a84994ccd0fa to 88e6a2d4-e7e3-4fbb-ac34-af441dc2a65e/bigFont/,
            0482d54c-aea9-4a46-b8a5-654489a92fe7 joins cb340c9b-d72d-4128-9dc9-4d2a7a04d2cc/window/@ad5cfc81-a6ab-45f6-9fb6-a84994ccd0fa to c8eefb73-a170-4e39-8573-41d4ef779cfb/window/,
            2c57859c-8842-4738-af34-1f0ab2738039 joins 7a10deea-97ca-4a58-b2c7-22fb523d98d8/bigFont/ to 0cd9b6f1-b65f-4856-a87d-9a94ae10a1d4/bigFont/@ad5cfc81-a6ab-45f6-9fb6-a84994ccd0fa;
    }

    component 8a5219b9-7620-48b5-94a5-d30636895492
         resembles 0eb329fa-5308-4cf6-ab3d-4278b8871da6/LTSA/ replaces 0eb329fa-5308-4cf6-ab3d-4278b8871da6/LTSA/
    {
        delete-parts:
            70c53cb8-8d67-489e-87f1-c7dbbc4b4b8b;
            7b4cc6e6-a427-4d3a-9bcb-293c8d94417a;
            f63e5e84-5630-4023-8202-6e26d0fb6acb;
            b39c341c-b874-43d5-b083-8fc2054299e8;
        parts:
            60b08d6b-d3e6-4df6-bb7b-05658636127f/df/: 3899efcd-c376-474b-b372-a143e0d076ea/DualWindowFactory/;
            c98857b5-c76d-4cf4-9cb0-1c49dbebde8f/dwm/: 09b3ed26-eec5-4a1b-acc3-591ffd05b4ec/WindowManager/
                fa5c68d3-29c1-40e6-82b3-c781fc9c4b05/name/ = "Combined Transitions and Alphabet!";
        replace-parts:
            1d816bd2-bbe3-4cf1-bd59-245dc418acb9 becomes 1d816bd2-bbe3-4cf1-bd59-245dc418acb9: 4fdbb4ba-5e56-4385-a083-e232b9ee5e36/HPWindow/
                f4cbd428-7cd0-4bbd-957f-292b791a4c6d/title/ = "Dual window LTSA",
                41201fc9-ad08-4a5b-a1d3-347f1a209adb/currentDirectory/ (10da413a-5619-4f34-9e9d-eddce57eb3ad/currentDirectory/);
                873ad06b-efd1-4b61-b577-331983f8c190/top/ (d33465a2-2f06-4eac-90a9-342dda5359b8/top/);
        delete-connectors:
            35a91b77-3eeb-4f27-bf5e-32ec6cef978a;
            a743dd64-33ce-4980-bbeb-d1bd3a92e4d6;
            7219f83a-27e9-4bd1-a344-61ed46331b35;
            2efe4430-f0ef-4624-bff7-9d2a0e00031a;
            c1ae66d4-3831-4cb1-bd43-88b50d368ecf;
            69093fb6-868a-4413-86df-7c80f4cd4877;
            8ad94065-8cc9-4a7b-9c73-595d33d51abe;
            400b2bc4-13d4-4ed9-b8fc-953b3815b979;
        connectors:
            fd332408-1b0c-4127-b695-6584b8eaba50 joins 8d5b54f3-5838-406a-aed7-fb67e332d624/create/@c98857b5-c76d-4cf4-9cb0-1c49dbebde8f/dwm/ to creator@60b08d6b-d3e6-4df6-bb7b-05658636127f/df/,
            3a4d116f-0586-49b4-9ab6-0ffcbd483a66 joins 0774ca16-3607-41a3-a6dd-b68726929228/window/@c98857b5-c76d-4cf4-9cb0-1c49dbebde8f/dwm/ to c8eefb73-a170-4e39-8573-41d4ef779cfb/window/@60b08d6b-d3e6-4df6-bb7b-05658636127f/df/,
            424caa91-6fb8-48b7-9b4f-093512058321 joins 7eed7894-beb1-4f59-81ad-b59999620bb9/proxy/@c98857b5-c76d-4cf4-9cb0-1c49dbebde8f/dwm/ to 22bfdfcc-d78c-4f45-85c2-107e5cb37144/window/@3a9fee35-0092-4e66-9584-98c404cb44a9,
            4f062371-eb3b-4758-9de6-4686f7201f68 joins 09562571-ef4b-4a50-a5e3-f9a42f44cbc9/options/@40f3df9f-b0d1-44a6-85f4-d7462ec7b58a to 88e6a2d4-e7e3-4fbb-ac34-af441dc2a65e/bigFont/@60b08d6b-d3e6-4df6-bb7b-05658636127f/df/,
            9184588c-5dc2-467b-8d86-ba3a1b33d53c joins 09562571-ef4b-4a50-a5e3-f9a42f44cbc9/options/@40f3df9f-b0d1-44a6-85f4-d7462ec7b58a to 7a10deea-97ca-4a58-b2c7-22fb523d98d8/bigFont/@60b08d6b-d3e6-4df6-bb7b-05658636127f/df/;
    }

}

