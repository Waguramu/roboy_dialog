.. _Configuration:

*************
Configuration
*************

Editing the Config File
=======================

Dialog System is configured using the ``config.properties`` file in the root of the project. 

ROS configuration
^^^^^^^^^^^^^^^^^

Dialog outsources many tasks to other modules implemented in Pyhton or C++ as ROS packages. In the config file you can enabled/disable ROS modules, choose which packages to use, and set the ``ROS_MASTER_URI``. 

Available ROS packages are:
    - ``roboy_gnlp`` (generative model for answer generation)
    - ``roboy_memory`` (Neo4j graph-based memory)
    - ``roboy_speech_synthesis`` (text to speech using Cerevoice)
    - ``roboy_speech_recognition`` (speech to text using Bing Speech API)
    - ``roboy_audio`` (audio source localization)
    - ``roboy_vision`` (face recogntion & object classification and localization)
    - ``roboy_face`` (triggers emotions)
    - ``roboy_filters`` (triggers snapchat-filters)

Example ROS config::

    ROS_ENABLED: true
    ROS_MASTER_IP: 10.183.49.162
    ROS_ACTIVE_PKGS:
      - roboy_memory
      - roboy_speech_synthesis



Inputs and Outputs
^^^^^^^^^^^^^^^^^^
   
A developer can choose how to interact with the dialog system. For example, for debugging purposes there are command line input and output. Importantly, there can be only one input, but many outputs. 

Available inputs are:
    - ``cmdline``
    - ``upd`` (listens for incoming udp packets in the port specified below)
    - ``bing`` (requires Internet connection and the ``roboy_speech_recognition`` ROS package)
    - ``telegram`` (requires Internet connection and a prepared telegram bot, see 1. Getting Started for more details. For the standard usecase, telegram should be set as both, in- and output.)
    
Arbitraty of the following outputs can be used simultaniously at the runtime::
    - ``cmdline``
    - ``cerevoice`` (requires ``roboy_speech_synthesis`` ROS package)
    - ``ibm`` (uses IBM Bluemix, requires Internet connection, user & pass configured below)
    - ``emotions`` (requires ``roboy_face`` ROS package)
    - ``udp`` (sends packets on the port configure below)
    - ``telegram`` (requires Internet connection and a prepared telegram bot, see :ref:`Installation` for more information. For the standard usecase, telegram should be set as both, in- and output.)

Example IO config::

    INPUT: cmdline
    OUTPUTS:
     - cmdline
     - ibm
     - cerevoice

Additional configuration from the "Utilities" paragraph may be required.

System behaviour flags
^^^^^^^^^^^^^^^^^^^^^^

Debug flag for en/disabling debug specific behaviour::

    DEBUG = false

Demo mode flag for en/disabling demo mode. This is for fairs and such where one would prepare the system for showing whilst giving a talk. ::

    DEMO_MODE: false

Infinite repitition flag: For input that require a single instance of the dialog system (like command line or on the roboy). En/disables beginning a new conversation after one has ended or ending the dialog system when the conversation has ended. ::

    INFINITE_REPETITION: true

Conversation timeout: The duration waited since the last action until a conversation is discarded in seconds ::

    CONVERSATION_TIMEOUT: 10800

Personality
^^^^^^^^^^^

Here you specify the state machine description store in the JSON file containing personality, i.e. states and transitions between them::

    PERSONALITY_FILE: "resources/personalityFiles/OrdinaryPersonality.json"
    
Utilities
^^^^^^^^^^
 
Configure third party communication ports, credentials, etc.

**UDP in-output** ::

    UDP_IN_SOCKET: 55555
    UDP_OUT_SOCKET: 55556
    UDP_HOST_ADDRESS: 127.0.0.1

**IBM Watson text-to-speech** ::

    IBM_TTS_USER: x
    IBM_TTS_PASS: x

**Telegram JSON-File path** (see :ref:`JSON Resources`) ::

    TELEGRAM_API_TOKENS_FILE: "/path/to/example.json"

.. _configuration_telegram_bot:

Configuring a telegram bot
==========================

If you'd like to use the ``telegram`` in- or output registering your own bot is necessary. Please proceed as follows:


1. Register a bot as described on the `telegram website <https://core.telegram.org/bots#3-how-do-i-create-a-bot>`_.

2. Place your telegram-bot authentification token in a JSON-File as described in :ref:`JSON Resources`.

3. Configure the Dialog System to use your file and to interact with the world via telegram as described above.


Enabling External APIs
===========================

Roboy Dialog supports multiple external APIs. At the present moment, three external API sources are provided. 

- OpenWeatherMap
- The Movie DB
- Yandex Translate

If you wish to have Roboy be able to tell the weather, tell you upcoming movies and translate, you have to register on these websites, generate an API key and denote the key in the ``resources/knowledgebase/apiKeys.yml`` file. There you shall replace the ``xxxxxxx`` portions with your key. 

.. warning:: 
    Do not upload any changes to the apiKey file to Git. Consequences will be severe...

Example
^^^^^^^^^^

Say my key were ``1234567890abc`` for all services, the YML file should look like

.. code-block:: yaml

    moviekey: 1234567890abc
    translatekey: 1234567890abc
    weatherkey: 1234567890abc

Assuming the key is valid, Roboy should now be able to answer the following questions:

- What is the weather like?
- What movies are playing?
- What is tree in french? [arbre]

.. seealso::
    See this link for more details on how to add your own APIs: :ref:`tut_ext_api`