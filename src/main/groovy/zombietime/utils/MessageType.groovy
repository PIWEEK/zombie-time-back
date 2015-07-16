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
    ATTACK,
    NOISE,
    SEARCH,
    SEARCH_MORE,
    GET_OBJECT,
    DISCARD_OBJECT,
    //GIVE_OBJECT,
    USE_OBJECT,
    EQUIP,
    UNEQUIP,
    END_TURN,

    //Both
    CHAT,
    DISCONNECT,

    /////////////////////////
    //  Output messages    //
    /////////////////////////
    //CHAT
    ANIMATION_MOVE,
    ANIMATION_ATTACK,
    FIND_ITEM,
    FULL_GAME,
    ZOMBIE_TIME,
    ZOMBIE_ATTACK,
    END_GAME,
    START_GAME,

}
