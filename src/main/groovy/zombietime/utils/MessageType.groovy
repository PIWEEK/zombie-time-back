package zombietime.utils

enum MessageType {
    /////////////////////////
    //  Input messages     //
    /////////////////////////
    //Pregame
    SELECT_SURVIVOR,
    PLAYER_READY,

    //Game
    MOVE,
    ATACK,
    NOISE,
    SEARCH,
    SEARCH_MORE,
    GET_OBJECT,
    GIVE_OBJECT,
    USE_OBJECT,
    EQUIP,
    END_TURN,

    //Both
    CHAT,
    DISCONNECT,

    /////////////////////////
    //  Output messages    //
    /////////////////////////
    //CHAT
    ANIMATION_LIGHTBOX,
    ANIMATION_MOVE,
    FULL_GAME,
    ZOMBIE_TIME,
    ZOMBIE_ATTACK,
    END_GAME,
    START_GAME,

}
