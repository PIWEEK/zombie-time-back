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
    UNEQUIP,


    //Both
    CHAT,
    DISCONNECT,
    END_TURN,

    /////////////////////////
    //  Output messages    //
    /////////////////////////
    //CHAT
    PRE_GAME,
    ANIMATION_MOVE,
    ANIMATION_NOISE,
    ANIMATION_ATTACK,
    FIND_ITEM,
    FULL_GAME,
    ZOMBIE_TIME,
    ZOMBIE_ATTACK,
    END_GAME,
    START_GAME,
    START_TURN

}
