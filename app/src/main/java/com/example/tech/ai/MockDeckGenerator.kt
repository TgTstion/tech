package com.example.tech.ai

import com.example.tech.data.Formality.FORMAL
import com.example.tech.data.Formality.INFORMAL
import com.example.tech.data.Formality.NEUTRAL
import com.example.tech.data.Formality.SLANG
import kotlinx.coroutines.delay

/**
 * Stand-in for the AI service (Milestone 1). Picks the sample bank that best matches the
 * topic, then tops up from the other banks if the learner asked for more cards than it holds.
 */
class MockDeckGenerator : DeckGenerator {

    override suspend fun generate(topic: String, count: Int): List<DraftCard> {
        delay(900) // simulate network latency so the loading state is visible in the demo
        val t = topic.lowercase()
        val primary = when {
            listOf("slang", "pub", "dublin", "irish", "casual").any { it in t } -> SLANG_BANK
            listOf("game", "gaming", "gamer", "esport").any { it in t } -> GAMING_BANK
            else -> IT_BANK
        }
        val rest = (IT_BANK + SLANG_BANK + GAMING_BANK) - primary.toSet()
        return (primary + rest).take(count)
    }

    private companion object {
        val IT_BANK = listOf(
            DraftCard("rollback", "/ˈrəʊlbæk/", NEUTRAL,
                "The deploy broke checkout, so we did a rollback to version 2.3.",
                "Always have a rollback plan before a database migration.",
                "\"I rollbacked the code.\" — 'rollback' is the noun; the verb is 'roll back'."),
            DraftCard("idempotent", "/ˌaɪdəmˈpəʊtənt/", FORMAL,
                "Make the payment endpoint idempotent so retries don't charge twice.",
                "PUT requests should be idempotent by design.",
                "\"This meeting is idempotent.\" — it describes repeatable operations, not boring events."),
            DraftCard("ship it", "/ʃɪp ɪt/", INFORMAL,
                "Tests are green — ship it!",
                "We can polish the icons later; let's ship it on Thursday.",
                "\"Please ship it to my flat.\" — here it means release software, not post a parcel."),
            DraftCard("technical debt", "/ˈteknɪkəl det/", NEUTRAL,
                "Skipping tests now will create technical debt we'll pay for later.",
                "We reserve one sprint per quarter to pay down technical debt.",
                "\"I owe the bank technical debt.\" — it is a metaphor for code shortcuts, not money."),
            DraftCard("blocker", "/ˈblɒkə/", INFORMAL,
                "The missing API key is a blocker for the release.",
                "At stand-up the lead asked, \"Any blockers?\"",
                "\"My blocker is that I'm hungry.\" — reserve it for issues that genuinely stop work."),
            DraftCard("LGTM", "/ˌel dʒiː tiː ˈem/", SLANG,
                "LGTM — approving the pull request now.",
                "Left two small comments, otherwise LGTM.",
                "Writing \"LGTM\" on code you haven't read — teammates take it as a real review."),
            DraftCard("hotfix", "/ˈhɒtfɪks/", NEUTRAL,
                "We pushed a hotfix for the login crash within an hour.",
                "Hotfixes go straight to production after a quick review.",
                "\"Let's hotfix the new feature next month.\" — a hotfix is urgent, not planned work."),
            DraftCard("spin up", "/spɪn ʌp/", INFORMAL,
                "I'll spin up a test server for the demo.",
                "You can spin up the whole stack locally with Docker.",
                "\"Can you spin up the meeting room?\" — it is used for computing resources."),
            DraftCard("deprecated", "/ˈdeprəkeɪtɪd/", FORMAL,
                "This method is deprecated; use the new API instead.",
                "Deprecated features will be removed in version 4.",
                "\"The library is depreciated.\" — 'depreciated' means it lost financial value."),
            DraftCard("bikeshedding", "/ˈbaɪkʃedɪŋ/", INFORMAL,
                "We spent an hour bikeshedding over the button colour.",
                "Let's stop bikeshedding and agree on the database first.",
                "\"I'm bikeshedding this weekend.\" — it means arguing about trivia, not building sheds."),
            DraftCard("edge case", "/ˈedʒ keɪs/", NEUTRAL,
                "An empty shopping cart is an edge case we forgot to test.",
                "Leap years are a classic edge case for date code.",
                "\"The edge case is in the corner of the office.\" — it is a rare input, not a place."),
            DraftCard("CI/CD pipeline", "/ˌsiː aɪ ˌsiː diː ˈpaɪplaɪn/", NEUTRAL,
                "Every commit runs through the CI/CD pipeline before release.",
                "The pipeline failed because a unit test timed out.",
                "\"The CI/CD pipeline is leaking.\" — it is an automated build process, not plumbing."),
        )

        val SLANG_BANK = listOf(
            DraftCard("grand", "/ɡrænd/", INFORMAL,
                "\"How are you?\" — \"Ah, grand, thanks.\"",
                "Don't worry about the delay, it'll be grand.",
                "Using 'grand' for 'magnificent' — in Ireland it usually just means 'fine'."),
            DraftCard("craic", "/kræk/", SLANG,
                "What's the craic? Anything new with you?",
                "The trad session last night was great craic.",
                "\"Can I get some craic?\" — it means fun or news, never anything illegal."),
            DraftCard("your round", "/jɔː raʊnd/", INFORMAL,
                "It's your round — mine's a pint, thanks.",
                "We take turns: I got the last round, so this is your round.",
                "\"Will you buy my round?\" — your round means you buy for the whole group."),
            DraftCard("sound", "/saʊnd/", SLANG,
                "He's a sound lad — he helped me move flat.",
                "\"See you at eight?\" — \"Sound.\"",
                "\"The soup is sound.\" — it describes decent people or means 'OK', not food quality."),
            DraftCard("gas", "/ɡæs/", SLANG,
                "That story about the taxi is gas!",
                "You're gas, you know that?",
                "\"I feel gas.\" — in Dublin 'gas' means funny, not bloated."),
            DraftCard("giving out", "/ˈɡɪvɪŋ aʊt/", INFORMAL,
                "My manager was giving out about the late report.",
                "Mam gave out to me for coming home at three.",
                "\"The teacher gave out me.\" — the pattern is 'give out TO someone'."),
            DraftCard("eejit", "/ˈiːdʒɪt/", SLANG,
                "Don't be an eejit, bring a jacket.",
                "I felt like an eejit when I missed the last bus.",
                "Calling your new manager an eejit — it is affectionate only among friends."),
            DraftCard("deadly", "/ˈdedli/", SLANG,
                "The gig on Saturday was deadly!",
                "You got the job? Deadly!",
                "\"Your proposal is deadly\" in a formal email — positive slang, wrong register."),
            DraftCard("story", "/ˈstɔːri/", SLANG,
                "\"Story, bud?\" — \"Not much, you?\"",
                "He walked in, nodded and said, \"Story, lads?\"",
                "Answering \"Story?\" with a long tale — it is a greeting, like 'what's up?'."),
            DraftCard("the jacks", "/ðə dʒæks/", SLANG,
                "Back in a minute, I'm going to the jacks.",
                "The jacks are at the back of the pub.",
                "Asking for 'the jacks' at a formal dinner — say 'the bathroom' there."),
            DraftCard("press", "/pres/", INFORMAL,
                "The mugs are in the press over the kettle.",
                "Put the cereal back in the press, please.",
                "Expecting non-Irish visitors to know it means 'cupboard' — add context."),
        )

        val GAMING_BANK = listOf(
            DraftCard("GG", "/ˌdʒiː ˈdʒiː/", SLANG,
                "GG, well played everyone.",
                "Close match — GG, see you next round.",
                "Typing \"GG\" mid-match while winning easily — it reads as mockery."),
            DraftCard("nerf", "/nɜːf/", INFORMAL,
                "The patch nerfed the sniper rifle's damage.",
                "That hero is too strong; they'll nerf her soon.",
                "\"The update nerfed my Wi-Fi.\" — it means a balance change inside the game."),
            DraftCard("buff", "/bʌf/", INFORMAL,
                "The healer got a big buff this season.",
                "Use your shield buff before the boss fight.",
                "\"I'm buffing my shoes.\" — fine in normal English, but not the gaming sense."),
            DraftCard("AFK", "/ˌeɪ ef ˈkeɪ/", SLANG,
                "Brb, AFK for two minutes.",
                "We lost because one teammate went AFK.",
                "Saying \"I'm AFK\" while still chatting — it means away from the keyboard."),
            DraftCard("clutch", "/klʌtʃ/", SLANG,
                "That one-versus-three clutch was unreal.",
                "She always plays clutch in the final round.",
                "\"I clutched it\" after losing — a clutch means succeeding under pressure."),
            DraftCard("noob", "/nuːb/", SLANG,
                "I'm still a noob at this game, go easy on me.",
                "Every pro was a noob once.",
                "Calling a teammate a noob in ranked — it is toxic; say 'new player'."),
            DraftCard("meta", "/ˈmetə/", INFORMAL,
                "Tank builds are meta this season.",
                "He ignores the meta and still wins.",
                "\"That film is so meta\" vs. the gaming sense — here it means the dominant strategy."),
            DraftCard("respawn", "/ˌriːˈspɔːn/", NEUTRAL,
                "You respawn at the base after ten seconds.",
                "Wait for me to respawn before you push.",
                "\"I need to respawn after this exam.\" — jokey; it means reappear in the game."),
            DraftCard("carry", "/ˈkæri/", INFORMAL,
                "She carried the whole team last night.",
                "Can you carry me through this raid?",
                "Claiming \"I carried\" after a weak game — it sounds arrogant to teammates."),
            DraftCard("lag", "/læɡ/", NEUTRAL,
                "Sorry, massive lag on my side.",
                "The server lag made aiming impossible.",
                "Blaming lag for every loss — it means network delay, not bad play."),
        )
    }
}
