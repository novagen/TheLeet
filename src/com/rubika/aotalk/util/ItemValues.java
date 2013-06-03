package com.rubika.aotalk.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.rubika.aotalk.service.ServiceTools;

import android.annotation.SuppressLint;
import android.util.Log;

@SuppressLint("UseSparseArrays")
public class ItemValues {
	private static final String APP_TAG = "--> The Leet :: ItemValues";

	private static Map<Integer, String> expansionList = new HashMap<Integer, String>() {
		private static final long serialVersionUID = 1L;
		{
			put(1, "Notum Wars");
			put(2, "Shadowlands");
			put(3, "Notum Wars, Shadow Lands");
			put(8, "Alien Invasion");
			put(32, "Lost Eden");
			put(128, "Legacy of Xan");
		}
	};
	
	private static Map<Integer, String> factionList = new HashMap<Integer, String>() {
		private static final long serialVersionUID = 2L;
		{
			put(0, "Neutral");
			put(1, "Clan");
			put(2, "Omni");
		}
	};
	
	private static Map<Integer, String> breedList = new HashMap<Integer, String>() {
		private static final long serialVersionUID = 3L;
		{
			put(1, "Solitus");
			put(2, "Opifex");
			put(3, "Nanomage");
			put(4, "Atrox");
		}
	};
	
	private static Map<Integer, String> expansionPlayfieldList = new HashMap<Integer, String>() {
		private static final long serialVersionUID = 4L;
		{
			put(0, "Rubi-Ka");
			put(1, "ShadowLands");
			put(6011, "Arid Rift");
		}
	};
	
	private static Map<Integer, String> currentPlayfieldList = new HashMap<Integer, String>() {
		private static final long serialVersionUID = 13L;
		{
			put(6011, "Arid Rift");
		}
	};
		
	
	private static Map<Integer, String> nanoStrainList = new HashMap<Integer, String>() {
		private static final long serialVersionUID = 5L;
		{
			put(0, "NO STACKING");
			put(1, "DamageShields");
			put(10, "DOT_NanotechnicianTypeB");
			put(100, "General_PharmaceuticalBuff");
			put(101, "General_PiercingBuff");
			put(102, "General_PiercingDebuff");
			put(103, "General_PistolBuff");
			put(104, "General_PistoDebuff");
			put(105, "General_PoisonACBuff");
			put(106, "General_ProjectileACBuff");
			put(107, "General_PsychologyBuff");
			put(108, "General_PsyModBuff");
			put(109, "General_PsyModDebuff");
			put(11, "NanotechnicianHaloNanoDebuff");
			put(110, "General_RadiationACBuff");
			put(111, "General_HPRegeneration");
			put(112, "General_RifleBuff");
			put(113, "General_RifleDebuff");
			put(114, "General_RiposteBuff");
			put(115, "General_RiposteDebuff");
			put(116, "General_SenseImpBuff");
			put(117, "General_SenseImpDebuff");
			put(118, "General_ShotgunBuff");
			put(119, "General_ShotgunDebuff");
			put(12, "HealOverTime");
			put(120, "General_SneakAttackBuff");
			put(121, "General_SneakAttackDebuff");
			put(122, "General_NanoACDebuff");
			put(123, "General_PoisonACDebuff");
			put(124, "General_SwimBuff");
			put(125, "General_TreatmentBuff");
			put(126, "General_TutoringBuff");
			put(127, "General_ChemicalACDebuff");
			put(128, "General_ColdACDebuff");
			put(129, "General_EnergyACDebuff");
			put(13, "Blindness");
			put(130, "General_FireACDebuff");
			put(131, "General_MeleeACDebuff");
			put(132, "General_ProjectileACDebuff");
			put(133, "General_RadiationACDebuff");
			put(134, "General_WeaponSmithingBuff");
			put(135, "Trader_SkillTransferTargetDebuff_Deprive");
			put(136, "Trader_SkillTransferTargetDebuff_Ransack");
			put(137, "Trader_SkillTransferCasterBuff_Deprive");
			put(138, "Trader_SkillTransferCasterBuff_Ransack");
			put(139, "Trader_ACTransferTargetDebuff_Siphon");
			put(14, "HumidityNPExtractor");
			put(140, "Trader_ACTransferTargetDebuff_Draw");
			put(141, "Trader_ACTransferCasterBuff_Siphon");
			put(142, "Trader_ACTransferCasterBuff_Draw");
			put(143, "Trader_ACTransferTargetBuff_Redeem");
			put(144, "MajorEvasionBuffs");
			put(145, "Snare");
			put(146, "Root");
			put(147, "MezzStun");
			put(148, "NPCostModifiers");
			put(149, "General_RunspeedBuffs");
			put(15, "XPBonus");
			put(150, "RunspeedBuffs");
			put(151, "HPBuff");
			put(152, "InitiativeBuffs");
			put(153, "2HEdgedWeaponsBuff");
			put(154, "BrawlBuff");
			put(155, "RiposteBuff");
			put(156, "StrengthBuff");
			put(157, "MatMetBuff");
			put(158, "MatMetDebuff");
			put(159, "MatCreaBuff");
			put(16, "General_1HandBluntBuff");
			put(160, "MatCreaDebuff");
			put(161, "MatLocBuff");
			put(162, "MatLocDebuff");
			put(163, "BioMetBuff");
			put(164, "BioMetDebuff");
			put(165, "SenseImpBuff");
			put(166, "SenseImpDebuff");
			put(167, "PsyModBuff");
			put(168, "PsyModDebuff");
			put(169, "PsychicDebuff");
			put(17, "General_1HandBluntDebuff");
			put(170, "IntelligenceDebuff");
			put(171, "FixerBreakEntryCombo");
			put(172, "ElectricalEngineeringBuff");
			put(173, "FieldQuantumPhysicsBuff");
			put(174, "MechanicalEngineeringBuff");
			put(175, "PharmaceuticalsBuff");
			put(176, "WeaponSmithingBuff");
			put(177, "ComputerLiteracyBuff");
			put(178, "NPBuff");
			put(179, "1HBluntBuff");
			put(18, "General_AimedShotBuff");
			put(180, "1HBlunt_2HBluntComboBuff");
			put(181, "NFRangeBuff");
			put(182, "CriticalIncreaseBuff");
			put(183, "InterruptModifier");
			put(184, "DoctorHPBuffs");
			put(185, "DoctorHPBuffsShort");
			put(186, "DoctorInitDebuffLine");
			put(187, "MetaPhysicistDamageDebuff");
			put(188, "MongoBuff");
			put(189, "EnforcerRage");
			put(19, "General_AimedShotDebuff");
			put(190, "FirstAidAndTreatmentBuffs");
			put(191, "PerceptionBuff");
			put(192, "SenseBuff");
			put(193, "ConcealmentBuff");
			put(194, "RifleBuff");
			put(195, "AgilityBuff");
			put(196, "ChemistryPharmBuff");
			put(197, "EvasionDebuffs");
			put(198, "AimedShotBuff");
			put(199, "PistolBuff");
			put(2, "ReflectShields");
			put(20, "General_AirTransportBuff");
			put(200, "PsychologyBuff");
			put(201, "NanoDeltaBuff");
			put(202, "CharmOther");
			put(203, "HealDeltaBuff");
			put(204, "NanoACBuff");
			put(206, "BreakingEntryAndDisarmTrapsBuff");
			put(207, "GrenadeBuff");
			put(208, "SneakAttackBuff");
			put(209, "MartialArtsBuff");
			put(21, "General_1HEdgedBuff");
			put(210, "NanoProgrammingBuff");
			put(211, "NPCostDebuff");
			put(212, "AssaultRifleBuff");
			put(213, "LREnergyWeaponBuff");
			put(214, "BurstBuff");
			put(215, "Trader_NPLeech");
			put(216, "MPPetDamageBuffs");
			put(217, "MPPetInitiativeBuffs");
			put(218, "Agent_ProfessionSwitch");
			put(219, "AbsorbAC");
			put(22, "General_1HEdgedDebuff");
			put(220, "Trader_TeamSkillWranglerBuff");
			put(221, "Metaphysicist_MindDamageNanoDebuffs");
			put(222, "MA_SuperdamageBuffs");
			put(223, "Adventurer_Polymorphs");
			put(224, "Nanotechnician_Fortify");
			put(225, "Metaphysicist_Anima");
			put(226, "ElianSoulLine");
			put(227, "EngineerAuras");
			put(228, "EngineerAura-Armour");
			put(229, "EngineerAura-DamageBuff");
			put(23, "General_2HBluntBuff");
			put(230, "EngineerAura-DamageShieldBuff");
			put(231, "EngineerAura-ReflectionDamageBuff");
			put(232, "PetTauntBuffing");
			put(233, "BureaucratSpeechLine");
			put(234, "BureacratMotivationalSpeechEffect");
			put(235, "DisarmTrapBuff");
			put(236, "EngineerDebuffAuras");
			put(237, "BureaucratMotivationalSpeechNanoACBuff");
			put(238, "BureaucratDemotivationalSpeeches");
			put(239, "NanoShutdownDebuff");
			put(24, "General_2HBluntDebuff");
			put(240, "AgentSuperCriticalLine");
			put(241, "AgentSureShotCriticalLine");
			put(242, "AgentExecutionerBuff");
			put(243, "AdventurerDamageShieldUpgrades");
			put(244, "1HEdgedBuff");
			put(245, "MultiwieldBuff");
			put(246, "MartialArtistControlledRageBuff");
			put(247, "AdventurerTarasqueHPBuff");
			put(248, "AdventurerLickWounds");
			put(249, "AdventurerPackHunterBase");
			put(25, "General_2HEdgedBuff");
			put(250, "AdventurerPackHunterBuffSegments");
			put(251, "AdventurerWolfVisionBuff");
			put(252, "AdventurerSabretoothDamageBuff");
			put(253, "FixerSuppressorBuff");
			put(254, "ChestBuffLine");
			put(255, "FixerLongHoTBuff");
			put(256, "Fear");
			put(257, "FixerNCUBuff");
			put(258, "TraderTeamHeals1");
			put(259, "TraderTeamHeals2");
			put(26, "General_2HEdgedDebuff");
			put(260, "TraderTeamHeals3");
			put(261, "TraderTeamHeals4");
			put(262, "TraderTeamHeals5");
			put(263, "TraderTeamHeals6");
			put(264, "TraderTeamHeals7");
			put(265, "TraderTeamHeals8");
			put(266, "TraderTeamHeals9");
			put(267, "TraderTeamHeals10");
			put(268, "TraderTeamHeals11");
			put(269, "TraderTeamHeals12");
			put(27, "General_AssaultRifleBuff");
			put(270, "TraderTeamHeals13");
			put(271, "TraderTeamHeals14");
			put(272, "TraderTeamHeals15");
			put(273, "TraderTeamHeals16");
			put(274, "TraderTeamHeals17");
			put(275, "johansnanoline");
			put(276, "TowerSmokeBuffEffects");
			put(277, "DroneTowerBuffEffects");
			put(278, "EnforcerPiercingBuff");
			put(279, "EnforcerMeleeEnergyBuff");
			put(28, "General_AssaultRifleDebuff");
			put(280, "SoldierShotgunBuff");
			put(281, "SoldierFullAutoBuff");
			put(282, "CompleteHealingLine");
			put(283, "AdventurerSelfRootSnareResistBuff");
			put(284, "AdventurerOtherRootSnareResistBuff");
			put(285, "PetSnareRootResistanceBuff");
			put(286, "EngineerSelfSpecialAttackAbsorber");
			put(287, "DoctorRansackDepriveResistBuff");
			put(288, "EngineerPetAOESnareBuff");
			put(289, "TemporalChaliceVisualEffectBuff");
			put(29, "General_AgilityBuff");
			put(290, "TeporaryRootSnareResistanceBuff");
			put(291, "MongoHoTComponent");
			put(292, "UnhallowedForceLine");
			put(293, "BeaconWarpLine");
			put(294, "BurntOutArmorProc");
			put(295, "HellGunDispelProc");
			put(296, "PerkLimber");
			put(297, "PerkDanceOfFools");
			put(298, "PerkChemicalBlindness");
			put(299, "PerkPoisonSprinkle");
			put(3, "ArmourBuff");
			put(30, "General_IntelligenceBuff");
			put(300, "PerkSealWounds");
			put(301, "PerkTranquilizer");
			put(302, "PerkToxicShock");
			put(303, "PerkConcussiveShot");
			put(304, "PerkAssasinate");
			put(305, "PerkBattlegroupHeal1");
			put(306, "PerkBattlegroupHeal2");
			put(307, "PerkViralCombination");
			put(308, "PerkBattlegroupHeal3");
			put(309, "PerkBattlegroupHeal4");
			put(31, "General_PsychicBuff");
			put(310, "PerkBioShield");
			put(311, "PerkBioCocoon");
			put(312, "PerkBioRejuvenation");
			put(313, "PerkBioRegrowth");
			put(314, "PerkChaoticModulation");
			put(315, "PerkSoftenUp");
			put(316, "PerkPinpointStrike");
			put(317, "PerkDeathStrike");
			put(318, "PerkLayOnHands");
			put(319, "PerkDevotionalArmor");
			put(32, "General_SenseBuff");
			put(320, "PerkCuringTouch");
			put(321, "PerkQuickBash");
			put(322, "PerkCrushBone");
			put(323, "PerkBringThePain");
			put(324, "PerkDevastatingBlow");
			put(325, "PerkBigSmash");
			put(326, "PerkFollowupSmash");
			put(327, "PerkBlindsideBlow");
			put(328, "PerkBureaucraticShuffle");
			put(329, "PerkSuccumb");
			put(33, "General_StaminaBuff");
			put(330, "PerkConfoundWithRules");
			put(331, "PerkEvasiveStance");
			put(332, "PerkElementaryTeleportation1");
			put(333, "PerkElementaryTeleportation2");
			put(334, "PerkElementaryTeleportation3");
			put(335, "PerkElementaryTeleportation4");
			put(336, "PerkICCNodeTeleportation");
			put(337, "PerkChannelRage");
			put(338, "PerkBlessingOflife");
			put(339, "PerkLifeblood");
			put(34, "General_StrengthBuff");
			put(340, "PerkDrawBlood");
			put(341, "PerkInstallExplosiveDevices");
			put(342, "PerkInstallNotumDepletiondevice");
			put(343, "PerkSuppressivePrimer");
			put(344, "PerkThermalPrimer");
			put(345, "PerkLeadership");
			put(346, "PerkGovernance");
			put(347, "PerkTheDirector");
			put(348, "PerkBalanceOfYinandYang");
			put(349, "PerkReapLife");
			put(35, "General_BioMetBuff");
			put(350, "PerkBloodletting");
			put(351, "PerkVitalShock");
			put(352, "PerkQuickCut");
			put(353, "PerkFlay");
			put(354, "PerkFlurryofCuts");
			put(355, "PerkRibbonFlesh");
			put(356, "PerkReconstructDNA");
			put(357, "PerkViralVipe");
			put(358, "PerkBreachDefenses");
			put(359, "PerkNanoHeal");
			put(36, "General_BioMetDebuff");
			put(360, "PerkExplorationTeleportation1");
			put(361, "PerkExplorationTeleportation2");
			put(362, "PerkDevour");
			put(363, "PerkBleedingWounds");
			put(364, "PerkGuttingBlow");
			put(365, "PerkHeal");
			put(366, "PerkInvocation");
			put(367, "PerkTrollForm");
			put(368, "PerkDisableNaturalHealing");
			put(369, "PerkStonefist");
			put(37, "General_BowBuff");
			put(370, "PerkAvalanche");
			put(371, "PerkGrasp");
			put(372, "PerkBearhug");
			put(373, "PerkGripofColossus");
			put(374, "PerkRemoval1");
			put(375, "PerkRemoval2");
			put(376, "PerkPurge1");
			put(377, "PerkPurge2");
			put(378, "PerkGreatPurge");
			put(379, "PerkReconstruction");
			put(38, "General_BowDebuff");
			put(380, "PerkTauntBos");
			put(381, "PerkSiphonLife");
			put(382, "PerkChaoticEnergy");
			put(383, "PerkRegainNano");
			put(384, "PerkNCUBooster");
			put(385, "PerkLaserPaintTarget");
			put(386, "PerkWeaponBash");
			put(387, "PerkTriangulateTarget");
			put(388, "PerkNapalmSpray");
			put(389, "PerkMarkofVengeance");
			put(39, "General_BowSpecialBuff");
			put(390, "PerkMarkofSufferance");
			put(391, "PerkMarkoftheUnclean");
			put(392, "PerkMarkoftheUnhallowed");
			put(393, "PerkArmorPiercingShot");
			put(394, "PerkFindtheFlaw");
			put(395, "PerkCalledShot");
			put(396, "PerkTremorHand");
			put(397, "PerkHarmonizeBodyandMind");
			put(398, "PerkTaunt");
			put(399, "PerkCharge");
			put(4, "DamageBuffing");
			put(40, "General_BowSpecialDebuff");
			put(400, "PerkHeadbutt");
			put(401, "PerkHatred");
			put(402, "PerkGroinKick");
			put(403, "PerkDeconstruction");
			put(404, "PerkEncaseinStone");
			put(405, "PerkDetonateStoneWorks");
			put(406, "PerkShutdownRemoval1");
			put(407, "PerkShutdownRemoval2");
			put(408, "PerkHeal");
			put(409, "PerkMaliciousProhibition");
			put(41, "General_BrawlBuff");
			put(410, "PerkTeamHeal");
			put(411, "PerkTreatmentTransfer");
			put(412, "PerkZapNano");
			put(413, "PerkNanoShakes");
			put(414, "PerkStripNano");
			put(415, "PerkAnnihilateNotumMolecules");
			put(416, "PerkFadeAnger");
			put(417, "PerkTapNotumSource");
			put(418, "PerkAccessNotumSource");
			put(419, "PerkBlastNano");
			put(42, "General_BrawlDebuff");
			put(420, "PerkStopNotumFlow");
			put(421, "PerkNotumOverflow");
			put(422, "PerkStoneworks");
			put(423, "PerkCripplePsyche");
			put(424, "PerkShatterPsyche");
			put(425, "PerkDominator");
			put(426, "PerkStab");
			put(427, "PerkDoubleStab");
			put(428, "PerkPerforate");
			put(429, "PerkLacerate");
			put(43, "General_BreakEntryBuff");
			put(430, "PerkImpale");
			put(431, "PerkGore");
			put(432, "PerkHecatomb");
			put(433, "PerkQuickShot");
			put(434, "PerkDoubleShot");
			put(435, "PerkDeadeye");
			put(436, "PerkEnergize");
			put(437, "PerkPowerVolley");
			put(438, "PerkPowerShock");
			put(439, "PerkPowerBlast");
			put(44, "General_BurstBuff");
			put(440, "PerkPowerCombo");
			put(441, "PerkAtrophy");
			put(442, "PerkDoomTouch");
			put(443, "PerkSpiritDissolution");
			put(444, "PerkFadeArmor");
			put(445, "PerkShadowBullet");
			put(446, "PerkNightKiller");
			put(447, "PerkShadowStab");
			put(448, "PerkBladeofNight");
			put(449, "PerkShadowKiller");
			put(45, "General_BurstDebuff");
			put(450, "PerkSnipeShot1");
			put(451, "PerkSnipeShot2");
			put(452, "PerkLegShot");
			put(453, "PerkEasyShot");
			put(454, "PerkReinforceSlugs");
			put(455, "PerkJarringBurst");
			put(456, "PerkSolidSlug");
			put(457, "PerkNeutroniumSlug");
			put(458, "PerkFieldBandage");
			put(459, "PerkTracer");
			put(46, "General_ChemicalACBuff");
			put(460, "PerkContainedBurst");
			put(461, "PerkViolence");
			put(462, "PerkGuardian");
			put(463, "PerkCure");
			put(464, "PerkVaccinate");
			put(465, "PerkCure2");
			put(466, "PerkVaccinate2");
			put(467, "PerkHaleandHearty");
			put(468, "PerkTeamHaleandHearty");
			put(469, "PerkCaptureVigor");
			put(47, "General_ChemistryBuff");
			put(470, "PerkUnhealedBlight");
			put(471, "PerkCaptureEssence");
			put(472, "PerkUnsealedPestilence");
			put(473, "PerkCaptureSpirit");
			put(474, "PerkUnsealedContagation");
			put(475, "PerkCaptureVitality");
			put(476, "PerkBane");
			put(477, "PerkDragonfire");
			put(478, "PerkChiConductor");
			put(479, "PerkIncapacitate");
			put(48, "General_ClimbBuff");
			put(480, "PerkFleshQuiver");
			put(481, "PerkOboliterate");
			put(482, "PerkDazzlewithLights");
			put(483, "PerkCombust");
			put(484, "PerkThermalDetonation");
			put(485, "PerkSupernova");
			put(486, "PerkDeepCuts");
			put(487, "PerkBladeWhirlwind");
			put(488, "PerkHonoringTheAncients");
			put(489, "PerkSeppukuSlash");
			put(49, "General_ColdACBuff");
			put(490, "PerkExultation");
			put(491, "PerkEtheralTouch");
			put(492, "PerkDimensionalFist");
			put(493, "PerkDisorient");
			put(494, "PerkConvulsiveTremor");
			put(495, "PerkSymbiosis");
			put(496, "PerkMaliciousSymbiosis");
			put(497, "PerkMalevolentSymbiosis");
			put(498, "PerkChtonianSymbiosis");
			put(499, "PerkQuarkContainmentField");
			put(5, "EnforcerChallenger");
			put(50, "General_ComputerLiteracyBuff");
			put(500, "PerkAccelerateDecayingQuarks");
			put(501, "PerkKnowledgeEnhancer");
			put(502, "PerkEscape");
			put(503, "PerkSabotageQuarkField");
			put(504, "PerkIgnitionFlare");
			put(505, "PerkRitualofDevotion");
			put(506, "PerkDevourVigor");
			put(507, "PerkRitualofZeal");
			put(508, "PerkDevourEssence");
			put(509, "PerkRitualofSpirit");
			put(51, "General_ConcealmentBuff");
			put(510, "PerkDevourVitality");
			put(511, "PerkRitualofBlood");
			put(512, "PerkECM1");
			put(513, "PerkECM2");
			put(514, "PerkSPECIALacrobat");
			put(515, "PerkSPECIALbureaucraticshuffle");
			put(516, "PerkSPECIALpersuader");
			put(517, "PerkSPECIALalchemist");
			put(518, "KeeperParryRiposteBuff");
			put(519, "KeeperFastAttackSneakAttackBuff");
			put(52, "General_DimachDebuff");
			put(520, "ShadeDamageProc-DamageInflictSegment");
			put(521, "ShadeProcBuff");
			put(522, "ShadeHPNPDoTProc-DamageInflictSegment");
			put(523, "ShadeInitDebuffProc");
			put(524, "KeeperSanctifierProc-DamageInflictSegment");
			put(525, "KeeperReaperProc-DamageInflictSegment");
			put(526, "KeeperProcBuff");
			put(527, "KeeperAura1-HPandNPHeal");
			put(528, "KeeperAura2-AbsorbReflectAMSBuff");
			put(529, "KeeperAura3-DamageSnareReductionBuff");
			put(53, "General_AgilityDebuff");
			put(530, "KeeperHealAura-Team");
			put(531, "KeeperNPHealAura-Team");
			put(532, "KeeperAbsorbAura-Team");
			put(533, "KeeperAMSDMSAura-Team");
			put(534, "KeeperReflectAura-Team");
			put(535, "KeeperDamageAura-Team");
			put(536, "KeeperSnareReductionAura-Team");
			put(537, "PerkSPECIALAssasin");
			put(538, "PerSPECIALEvasicestance");
			put(539, "KeeperStr/Stam/AgiBuff");
			put(54, "General_IntelligenceDebuff");
			put(540, "PerkSPECIALTinkerer");
			put(541, "PerkSpecialThief");
			put(542, "PerkSPECIALStarfall");
			put(543, "PerSpecialShadowsneak");
			put(544, "PerkSpecialKungfuMaster");
			put(545, "KeeperEvadeDodgeDuckBuff");
			put(546, "ShadePiercingBuff");
			put(547, "DimachBuff");
			put(548, "PerkAuraOfRevival-HealStopperBuff");
			put(549, "PerkCommandingPresenceBuff");
			put(55, "General_PsychicDebuff");
			put(550, "PerkDirectorshipBuff");
			put(551, "PerkChannelingOfNotum-HealStopperBuff");
			put(552, "PerkTheoreticalResearch");
			put(553, "PerkStreetSamurai");
			put(554, "PerkSpecialForces");
			put(555, "PerkSMGMastery");
			put(556, "PerkNanoSurgeon");
			put(557, "PerkHeavyRanged");
			put(558, "PerkGridNCU");
			put(559, "PerkEnhancedNanoDamage");
			put(56, "General_SenseDebuff");
			put(560, "mariusgmnano");
			put(561, "Perk Nano Surgeon");
			put(562, "UNUSED 2");
			put(563, "General Dimach Buff");
			put(564, "General Melee Multiple Buff");
			put(565, "MonsterWaveSpawn1");
			put(566, "MonsterWaveSpawn2");
			put(567, "MonsterWaveSpawn3");
			put(568, "MonsterWaveSpawn4");
			put(569, "MonsterWaveSpawn5");
			put(57, "General_StaminaDebuff");
			put(570, "MonsterWaveSpawn6");
			put(571, "MonsterWaveSpawn7");
			put(572, "MonsterWaveSpawn8");
			put(573, "MonsterWaveSpawn9");
			put(574, "MonsterWaveSpawn10");
			put(575, "Battlegroup Heal");
			put(576, "Psy/Int Buff");
			put(577, "Bio Shielding");
			put(578, "Bio Cocoon");
			put(579, "Bio Rejuvenation");
			put(58, "General_StrengthDebuff");
			put(580, "Bio Regrowth");
			put(581, "General Ranged Multiple Buff");
			put(582, "DOT Strain C");
			put(583, "Devotional Armor");
			put(584, "Scale Repair");
			put(585, "Slobber Wounds");
			put(586, "Lick Wounds NA");
			put(587, "SL Nanopoint Drain");
			put(588, "Nano Point Heals");
			put(589, "Blessing of Life");
			put(59, "General_DisarmTrapsBuff");
			put(590, "Lifeblood");
			put(591, "Draw Blood");
			put(592, "Soldier Heavy Weapons Buff");
			put(593, "Ethereal Touch");
			put(594, "Convulsive Tremor");
			put(595, "Nano Recharge");
			put(596, "Health Recharge");
			put(597, "Enforcer Damage Change Series");
			put(598, "Bonfire Recharger");
			put(599, "Ritual of Devotion");
			put(6, "DOT_DoctorTypeA");
			put(60, "General_ElectricalEngineeringBuff");
			put(600, "Ritual of Zeal");
			put(601, "Ritual of Spirit");
			put(602, "Ritual of Blood");
			put(603, "MonsterEffect1");
			put(604, "MonsterEffect2");
			put(605, "MonsterEffect3");
			put(606, "MonsterEffect4");
			put(607, "MonsterEffect5");
			put(608, "MonsterEffect6");
			put(609, "MonsterEffect7");
			put(61, "General_EnergyMeleeBuff");
			put(610, "MonsterEffect8");
			put(611, "Short Term XP Gain");
			put(612, "Double Stab Bleeding Wounds");
			put(613, "Lacerate Bleeding Wounds");
			put(614, "Gore Bleeding Wounds");
			put(615, "Hecatomb Bleeding Wounds");
			put(616, "MonsterEffect_Breakable");
			put(617, "MonsterEffect_DuringFight");
			put(618, "Perk Cleave");
			put(619, "Perk Transfix");
			put(62, "General_EnergyMeleeDebuff");
			put(620, "Perk Pain Lance");
			put(621, "Perk Slice And Dice");
			put(622, "Perk Pulverize");
			put(623, "Perk Hammer And Anvil");
			put(624, "Perk Overwhelming Might");
			put(625, "Perk Seismic Smash");
			put(626, "Pain Lance DoT");
			put(627, "Enforcer Taunt Procs");
			put(628, "Enforcer Taunt Procs Fearbringer");
			put(629, "Enforcer Taunt Procs Irebringer");
			put(63, "General_EnergyACBuff");
			put(630, "Enforcer Taunt Procs Wrathbringer");
			put(631, "Enforcer Taunt Procs Hatebringer");
			put(632, "Enforcer Taunt Procs Ragebringer");
			put(633, "Enforcer Taunt Procs Dreadbringer");
			put(634, "Accelerate Decaying Quarks Debuff");
			put(635, "Agent Damage Proc-DamageInflictSegment");
			put(636, "Agent Proc Buff");
			put(637, "MonsterEffect_MainLoop");
			put(638, "Atrophy");
			put(639, "Deep Cuts");
			put(64, "General_LREnergyWeaponBuff");
			put(640, "Trader Debuff AC Nanos");
			put(641, "Leg Shot");
			put(642, "Crush Bone");
			put(643, "Trader Debuff NanoAC Light");
			put(644, "Debuff NanoAC Heavy");
			put(645, "Called Shot Bleeding Wounds");
			put(646, "Energize");
			put(647, "Mark of Vengeance");
			put(648, "Mark of Sufferance");
			put(649, "Mark of the Unclean");
			put(65, "General_LREnergyWeaponDebuff");
			put(650, "Mark of the Unhallowed");
			put(651, "Toxic Shock");
			put(652, "Toxic Shock Proc Effect");
			put(653, "Dodge the Blame");
			put(654, "Confound with Rules");
			put(655, "Succumb");
			put(656, "Troll Form");
			put(657, "Disable Natural Healing");
			put(658, "MP Damage Debuff Line A");
			put(659, "MP Damage Debuff Line B");
			put(66, "General_FastAttackBuff");
			put(660, "Nano Shakes");
			put(661, "Tap Notum Source");
			put(662, "Blast Nano");
			put(663, "Stop Notum Flow");
			put(664, "Notum Overflow");
			put(665, "Blade of Night");
			put(666, "Violence");
			put(667, "Violence Controller");
			put(668, "Guardian");
			put(669, "Total Mirror Shield");
			put(67, "General_FastAttackDebuff");
			put(670, "Dazzle with Lights");
			put(671, "Knowledge Enhancer");
			put(672, "Bleeding Wounds");
			put(673, "Fixer Dodge Buff Line");
			put(674, "Hammer and Anvil");
			put(675, "Zap Nano");
			put(676, "Channel Rage");
			put(677, "Chaotic Modulation");
			put(678, "Freak Strength Stun");
			put(679, "Freak Strength Self Stun");
			put(68, "General_FieldQuantumPhysicsBuff");
			put(680, "Agent Escape Nanos");
			put(681, "Reconstruction");
			put(682, "Taunt Box");
			put(683, "Siphon Box");
			put(684, "Gadgeteer Pet Procs");
			put(685, "Groin Kick");
			put(686, "Reconstruction");
			put(687, "Taunt Box");
			put(688, "Siphon Box");
			put(689, "Deconstruction");
			put(69, "General_FireACBuff");
			put(690, "Install Explosive Device DoT");
			put(691, "Install Notum Depletion Device DoT");
			put(692, "Install Explosive Device Countdown");
			put(693, "Install Notum Depletion Device Countdown");
			put(694, "Shadowland Reflect Base");
			put(695, "Blackstep");
			put(696, "Obscure Vision");
			put(697, "Gather Darkness");
			put(698, "Silence");
			put(699, "Silence Debuff");
			put(7, "DOT_DoctorTypeB");
			put(70, "General_FirstAidBuff");
			put(700, "Misery");
			put(701, "Death");
			put(702, "Path of Darkness");
			put(703, "Path of Darkness Debuff");
			put(704, "Road To Darkness");
			put(705, "Road To Darkness Debuff");
			put(706, "The Choice (Omni)");
			put(707, "The Choice Debuff (Omni)");
			put(708, "Blackfist");
			put(709, "Slam of Darkness");
			put(71, "General_FlingShotBuff");
			put(710, "Slam of Darkness Debuff");
			put(711, "Scream of Death");
			put(712, "Scream of Death Debuff");
			put(713, "Lightstep");
			put(714, "Gather Light");
			put(715, "Rain of Light");
			put(716, "Rain of Light Buff");
			put(717, "Morning");
			put(718, "Morning Debuff");
			put(719, "Hope");
			put(72, "General_FlingShotDebuff");
			put(720, "Hope Buff");
			put(721, "Hope Debuff");
			put(722, "Life");
			put(723, "Path of Light");
			put(724, "Tunnel of Light");
			put(725, "Tunnel of Light Buff");
			put(726, "The Choice (Clan)");
			put(727, "Screen of Light");
			put(728, "Shield of Light");
			put(729, "Shield of Light Buff");
			put(73, "General_FullAutoBuff");
			put(730, "Fortress of Light");
			put(731, "Fortress of Light Buff");
			put(732, "Misery Buff");
			put(733, "Misery Debuff");
			put(734, "Quark Containment Field");
			put(735, "Fury");
			put(736, "Reinforced Slugs");
			put(737, "Affected by Nano Heal");
			put(738, "Shadowland Bind and Recall");
			put(739, "Performed Ritual of Devotion");
			put(74, "General_FullAutoDebuff");
			put(740, "Performed Ritual of Zeal");
			put(741, "Performed Ritual of Spirit");
			put(742, "Performed Ritual of Blood");
			put(743, "Performed Devour Vigor");
			put(744, "Performed Devour Essence");
			put(745, "Performed Devour Vitality");
			put(746, "Performed Stab");
			put(747, "Performed Perforate");
			put(748, "Performed Impale");
			put(749, "Performed Double Stab");
			put(75, "General_ThrownGrapplingBuff");
			put(750, "Performed Lacerate");
			put(751, "Performed Gore");
			put(752, "Performed Hecatomb");
			put(753, "Performed Capture Vigor");
			put(754, "Performed Capture Essence");
			put(755, "Performed Capture Spirit");
			put(756, "Performed Capture Vitality");
			put(757, "Affected by Taint Wounds");
			put(758, "Performed Unsealed Blight");
			put(759, "Performed Unsealed Pestilence");
			put(76, "General_ThrownGrapplingDebuff");
			put(760, "Performed Unsealed Contagion");
			put(761, "Transition Of Ergo");
			put(762, "Insurance Agent");
			put(763, "Insurance Claim");
			put(764, "Affected by Insurance Claim");
			put(765, "Regain Nano");
			put(766, "Grove Healing Multiplier");
			put(767, "Instinctive Control");
			put(768, "Special Attack Absorber Base");
			put(769, "Total Focus");
			put(77, "General_GrenadeBuff");
			put(770, "Soldier Damage Base");
			put(771, "Affected By Defensive Stance");
			put(772, "Defensive Stance");
			put(773, "Agent Detaunt Proc-Detaunt Segment");
			put(774, "Affected by Deceptive Stance");
			put(775, "Deceptive Stance");
			put(776, "Affected by Consume the Soul");
			put(777, "Short Term HP Buff");
			put(778, "Affected by Spirit of Blessing");
			put(779, "Affected by Spirit of Purity");
			put(78, "General_GrenadeDebuff");
			put(780, "Spirit of Blessing");
			put(781, "Spirit of Purity");
			put(782, "WaitForAttackEffectNano2");
			put(783, "DuringFightNanoEffect2");
			put(784, "Dance of Fools");
			put(785, "Environmental Damage");
			put(786, "Fixer Runspeed Base");
			put(787, "AIPERK Blur");
			put(788, "AIPERK Sacrifice");
			put(789, "MINI DoT");
			put(79, "General_GroundTransportBuff");
			put(790, "Zix Line");
			put(791, "AI AMSmodifier proc");
			put(792, "AIPERK Silent Plague");
			put(793, "AIPERK Insight");
			put(794, "AIPERK Assume Target");
			put(795, "Daring");
			put(796, "Leet Empower");
			put(797, "Link");
			put(798, "No Terraform");
			put(799, "Boss Root");
			put(8, "DOT_Nanotechnician");
			put(80, "General_MaxHealthBuff");
			put(800, "Cocoon");
			put(801, "NT Area Nukes");
			put(802, "AE Level Spawn");
			put(803, "Scones");
			put(804, "Privacy Shield");
			put(805, "Batter Up");
			put(806, "Armor Damage");
			put(807, "Healing Construct Empowerment");
			put(808, "PH");
			put(809, "Nano Shielding");
			put(81, "General_KnifeBuff");
			put(810, "Mesmerization Construct Empowerment");
			put(811, "Engineer Miniaturization");
			put(812, "Research Ability 1");
			put(813, "Research Ability 2");
			put(814, "Trader Drain AAO/AAD*");
			put(815, "MA Bow Buff*");
			put(816, "Pet Mezz Charm Resist*");
			put(817, "Pet Snare Resist*");
			put(818, "Advy Pit Lizard Support*");
			put(82, "General_KnifeDebuff");
			put(820, "Soldier Improved Automatic Targeting*");
			put(824, "Nullity Sphere*");
			put(825, "Epsilon Purge*");
			put(83, "General_SMGBuff");
			put(830, "True Profession*");
			put(831, "Engineer Shield of the Obedient Servant");
			put(833, "Restrict Movement1*");
			put(834, "Restrict Movement2*");
			put(835, "MA NR% Buff*");
			put(836, "Soldier Automatic Targeting*");
			put(84, "General_SMGDebuff");
			put(842, "Enforcer Mongo DeCrush*");
			put(843, "MP Pet HealDelta% Buff*");
			put(844, "Shade Sneaking Health Drain*");
			put(845, "Trader Drain Damage*");
			put(85, "General_MartialArtsBuff");
			put(851, "Prisoners Cloak Success/Failure*");
			put(852, "Will of the Reanimator*");
			put(854, "MP Attack Construct Empowerment*");
			put(855, "NT Izgimmer's Wealth*");
			put(856, "Trader Grand Theft Humidity*");
			put(86, "General_MartialArtsDebuff");
			put(860, "Malpractice (Debuff)*");
			put(861, "Shade Nanite Depravation*");
			put(863, "Touch of Poison*");
			put(868, "Agent Debuff Dodge");
			put(87, "General_MatCreaBuff");
			put(88, "General_MatCreaDebuff");
			put(883, "PvP Fear*");
			put(884, "PvP Knockback*");
			put(887, "Fear After Effect*");
			put(889, "Trader Shutdown Skills*");
			put(89, "General_MatLocBuff");
			put(893, "Crat Last Minute Negotiations*");
			put(9, "DOT_Agent");
			put(90, "General_MatLocDebuff");
			put(900, "PvP Fear Resist*");
			put(901, "Fear Resist*");
			put(902, "Veterans L33t*");
			put(91, "General_MatMetBuff");
			put(910, "PvP Enabled*");
			put(917, "Gravity Shift*");
			put(92, "General_MatMetDebuff");
			put(922, "Borrow Reflect*");
			put(93, "General_MechanicalEngineeringBuff");
			put(94, "General_MeleeACBuff");
			put(95, "General_NanoProgrammingBuff");
			put(96, "General_NanoACBuff");
			put(97, "General_NPRegeneration");
			put(98, "General_ParryBuff");
			put(99, "General_ParryDebuff");
		}
	};
	
	private static Map<Integer, String> perkList = new HashMap<Integer, String>() {
		private static final long serialVersionUID = 6L;
		{
			put(1250, "Alien Technology Expertise 1");
			put(1251, "Alien Technology Expertise 2");
			put(1252, "Alien Technology Expertise 3");
		}
	};
	
	private static Map<Integer, String> wornItemList = new HashMap<Integer, String>() {
		private static final long serialVersionUID = 7L;
		{
			put(16, "GridArmor");
			put(64, "Profession_Nanodeck");
		}
	};
	
	private static Map<Integer, String> flagList = new HashMap<Integer, String>() {
		private static final long serialVersionUID = 15L;
		{
			put(0, "Visible");
			put(1, "ModifiedDescription");
			put(2, "ModifiedName");
			put(3, "CanBeTemplateItem");
			put(4, "TurnOnUse");
			put(5, "HasMultipleCount");
			put(6, "Locked");
			put(7, "Open");
			put(8, "ItemSocialArmour");
			put(9, "TellCollision");
			put(10, "NoSelectionIndicator");
			put(11, "UseEmptyDestruct");
			put(12, "Stationary");
			put(13, "Repulsive");
			put(14, "DefaultTarget");
			put(15, "ItemTextureOverride");
			put(16, "Null");
			put(17, "HasAnimation");
			put(18, "HasRotation");
			put(19, "WantCollision");
			put(20, "WantSignals");
			put(21, "HasSentFirstIIR");
			put(22, "HasEnergy");
			put(23, "MirrorInLeftHand");
			put(24, "IllegalClan");
			put(25, "IllegalOmni");
			put(26, "NoDrop");
			put(27, "Unique");
			put(28, "CanBeAttacked");
			put(29, "DisableFalling");
			put(30, "HasDamage");
			put(31, "DisableStatelCollision");
		}
	};
	
	private static Map<Integer, String> slotWeaponsList = new HashMap<Integer, String>() {
		private static final long serialVersionUID = 8L;
		{
			/*
			put(0, "Whoops");
			put(1, "HUD 2");
			put(2, "HUD 3");
			put(3, "Utils 1");
			put(4, "Utils 2");
			put(5, "Utils 3");
			put(6, "Right hand");
			put(7, "Deck");
			put(8, "Left hand");
			put(9, "Deck 1");
			put(10, "Deck 2");
			put(11, "Deck 3");
			put(12, "Deck 4");
			put(13, "Deck 5");
			put(14, "Deck 6");
			put(15, "HUD 1");
			*/
			put(1, "HUD1");
			put(2, "HUD3");
			put(3, "UTIL1");
			put(4, "UTIL2");
			put(5, "UTIL3");
			put(6, "RightHand");
			put(7, "BELT");
			put(8, "LeftHand");
			put(9, "NCU1");
			put(10, "NCU2");
			put(11, "NCU3");
			put(12, "NCU4");
			put(13, "NCU5");
			put(14, "NCU6");
			put(15, "HUD2");

		}
	};
	
	private static Map<Integer, String> slotClothingList = new HashMap<Integer, String>() {
		private static final long serialVersionUID = 11L;
		{
			/*
			put(0, "Neck");
			put(1, "Head");
			put(2, "Back");
			put(3, "Left shoulder");
			put(4, "Chest");
			put(5, "Right shoulder");
			put(6, "Right arm");
			put(7, "Hands");
			put(8, "Left arm");
			put(9, "Right wrist");
			put(10, "Legs");
			put(11, "Left wrist");
			put(12, "Right finger");
			put(13, "Feet");
			put(14, "Left finger");
			*/
			put(1, "Neck");
			put(2, "Head");
			put(3, "Back");
			put(4, "RightShoulder");
			put(5, "Chest");
			put(6, "LeftShoulder");
			put(7, "RightArm");
			put(8, "Hands");
			put(9, "LeftArm");
			put(10, "RightWrist");
			put(11, "Legs");
			put(12, "LeftWrist");
			put(13, "RightFinger");
			put(14, "Feet");
			put(15, "LeftFinger");
		}
	};
	
	private static Map<Integer, String> slotImplantsList = new HashMap<Integer, String>() {
		private static final long serialVersionUID = 12L;
		{
			/*
			put(0, "Eyes");
			put(1, "Head");
			put(2, "Ears");
			put(3, "Right arm");
			put(4, "Chest");
			put(5, "Left arm");
			put(6, "Right wrist");
			put(7, "Waist");
			put(8, "Left wrist");
			put(9, "Right hand");
			put(10, "Legs");
			put(11, "Left hand");
			put(12, "Feet");
			*/
			put(1, "Eyes");
			put(2, "Head");
			put(3, "Ears");
			put(4, "RightArm");
			put(5, "Chest");
			put(6, "LeftArm");
			put(7, "RightWrist");
			put(8, "Waist");
			put(9, "LeftWrist");
			put(10, "RightHand");
			put(11, "Legs");
			put(12, "LeftHand");
			put(13, "Feet");

		}
	};
	
	private static Map<Integer, String> professionList = new HashMap<Integer, String>() {
		private static final long serialVersionUID = 9L;
		{
			put(1, "Soldier");
			put(2, "Martial Artist");
			put(3, "Engineer");
			put(4, "Fixer");
			put(5, "Agent");
			put(6, "Adventurer");
			put(7, "Trader");
			put(8, "Bureaucrat");
			put(9, "Enforcer");
			put(10, "Doctor");
			put(11, "Nano-Technician");
			put(12, "Meta-Physicist");
			put(14, "Keeper");
			put(15, "Shade");
		}
	};
	
	private static Map<Integer, String> skillList = new HashMap<Integer, String>() {
		private static final long serialVersionUID = 10L;
		{
			/*
			put(1, "MaxHealth");
			put(16, "Strength");
			put(17, "Agility");
			put(18, "Stamina");
			put(19, "Intelligence");
			put(20, "Sense");
			put(21, "Psychic");
			put(27, "Health");
			put(45, "BeltSlots");
			put(90, "ProjectileAC");
			put(91, "MeleeAC");
			put(92, "EnergyAC");
			put(93, "ChemicalAC");
			put(94, "RadiationAC");
			put(95, "ColdAC");
			put(96, "PoisonAC");
			put(97, "FireAC");
			put(100, "MartialArts");
			put(101, "MultiMelee");
			put(102, "1hBlunt");
			put(103, "1hEdged");
			put(104, "MeleeEnergy");
			put(105, "2hEdged");
			put(106, "Piercing");
			put(107, "2hBlunt");
			put(108, "SharpObjects");
			put(109, "Grenade");
			put(110, "HeavyWeapons");
			put(111, "Bow");
			put(112, "Pistol");
			put(113, "Rifle");
			put(114, "MG/SMG");
			put(115, "Shotgun");
			put(116, "AssaultRifle");
			put(117, "VehicleWater");
			put(118, "MeleeInit");
			put(119, "RangedInit");
			put(120, "PhysicalInit");
			put(121, "BowSpecialAttack");
			put(122, "SensoryImprovement");
			put(123, "FirstAid");
			put(124, "Treatment");
			put(125, "MechanicalEngineering");
			put(126, "ElectricalEngineering");
			put(127, "MaterialMetamorphose");
			put(128, "BiologicalMetamorphose");
			put(129, "PsychologicalModification");
			put(130, "MaterialCreation");
			put(131, "SpaceTime");
			put(132, "NanoPool");
			put(133, "RangedEnergy");
			put(134, "MultiRanged");
			put(135, "TrapDisarm");
			put(136, "Perception");
			put(137, "Adventuring");
			put(138, "Swimming");
			put(139, "VehicleAir");
			put(140, "MapNavigation");
			put(141, "Tutoring");
			put(142, "Brawl");
			put(143, "Riposte");
			put(144, "Dimach");
			put(145, "Parry");
			put(146, "SneakAttack");
			put(147, "FastAttack");
			put(148, "Burst");
			put(149, "NanoInit");
			put(150, "FlingShot");
			put(151, "AimedShot");
			put(152, "BodyDevelopment");
			put(153, "DuckExplosions");
			put(154, "DodgeRanged");
			put(155, "EvadeClose");
			put(156, "RunSpeed");
			put(157, "QuantumFT");
			put(158, "WeaponSmithing");
			put(159, "Pharmaceuticals");
			put(160, "NanoProgramming");
			put(161, "ComputerLiteracy");
			put(162, "Psychology");
			put(163, "Chemistry");
			put(164, "Concealment");
			put(165, "BreakingEntry");
			put(166, "VehicleGround");
			put(167, "FullAuto");
			put(168, "NanoResist");
			put(181, "MaxNCU");
			put(207, "ReflectEnergyAC");
			put(221, "MaxNanoEnergy");
			put(226, "ShieldProjectileAC");
			put(227, "ShieldMeleeAC");
			put(228, "ShieldEnergyAC");
			put(229, "ShieldChemicalAC");
			put(230, "ShieldRadiationAC");
			put(231, "ShieldColdAC");
			put(233, "ShieldFireAC");
			put(234, "ShieldPoisonAC");
			put(238, "AbsorbProjectileAC");
			put(239, "AbsorbMeleeAC");
			put(240, "AbsorbEnergyAC");
			put(241, "AbsorbChemicalAC");
			put(242, "AbsorbRadiationAC");
			put(243, "AbsorbColdAC");
			put(244, "AbsorbFireAC");
			put(245, "AbsorbPoisonAC");
			put(276, "AddAllOffense");
			put(277, "AddAllDefense");
			put(278, "ProjectileDamageModifier");
			put(280, "EnergyDamageModifier");
			put(281, "ChemicalDamageModifier");
			put(282, "RadiationDamageModifier");
			put(279, "MeleeDamageModifier");
			put(311, "ColdDamageModifier");
			put(316, "FireDamageModifier");
			put(317, "PoisonDamageModifier");
			put(318, "NanoCost");
			put(319, "XPModifier");
			put(343, "HealDelta");
			put(360, "Scale");
			put(364, "NanoDelta");
			put(379, "CriticalIncrease");
			put(380, "WeaponRange");
			put(381, "NanoRange");
			put(382, "SkillLockModifier");
			put(383, "NanoInterruptModifier");
			put(391, "CriticalResistance");
			put(477, "MaxReflectedEnergyAC");
			put(535, "HealModifier");
			put(536, "NanoDamage");
			put(566, "GaurdianOfShadows");
			*/
			put(0, "Flags");
			put(1, "MaxHealth");
			put(2, "Mass");
			put(3, "AttackSpeed");
			put(4, "Breed");
			put(5, "Organization");
			put(6, "Team");
			put(7, "State");
			put(8, "Duriation");
			put(9, "MapFlags");
			put(10, "ProfessionLevel");
			put(11, "PreviousHealth");
			put(12, "Mesh");
			put(13, "Anim");
			put(14, "Name");
			put(15, "Info");
			put(16, "Strength");
			put(17, "Agility");
			put(18, "Stamina");
			put(19, "Intelligence");
			put(20, "Sense");
			put(21, "Psychic");
			put(22, "AMS");
			put(23, "StaticInstance");
			put(24, "MaxMass");
			put(25, "StaticType");
			put(26, "Energy");
			put(27, "Health");
			put(28, "Height");
			put(29, "DMS");
			put(30, "Can");
			put(31, "Face");
			put(32, "HairMesh");
			put(33, "Faction");
			put(34, "DeadTimer");
			put(35, "AccessCount");
			put(36, "AttackCount");
			put(37, "TitleLevel");
			put(38, "BackMesh");
			put(39, "ShoulderMesh");
			put(40, "AlienXP");
			put(41, "FabricType");
			put(42, "CATMesh");
			put(43, "ParentType");
			put(44, "ParentInstance");
			put(45, "BeltSlots");
			put(46, "BandolierSlots");
			put(47, "Girth");
			put(48, "ClanLevel");
			put(49, "InsuranceTime");
			put(50, "InventoryTimeout");
			put(51, "AggDef");
			put(52, "XP");
			put(53, "IP");
			put(54, "Level");
			put(55, "InventoryId");
			put(56, "TimeSinceCreation");
			put(57, "LastXP");
			put(58, "Age");
			put(59, "Gender");
			put(60, "Profession");
			put(61, "Credits");
			put(62, "Alignment");
			put(63, "Attitude");
			put(64, "HeadMesh");
			put(65, "HairTexture");
			put(66, "ShoulderTexture");
			put(67, "HairColourRGB");
			put(68, "NumConstructedQuest");
			put(69, "MaxConstructedQuest");
			put(70, "SpeedPenalty");
			put(71, "TotalMass");
			put(72, "ItemType");
			put(73, "RepairDifficulty");
			put(74, "Value");
			put(75, "NanoStrain");
			put(76, "EquipmentPage");
			put(77, "RepairSkill");
			put(78, "CurrentMass");
			put(79, "Icon");
			put(80, "PrimaryItemType");
			put(81, "PrimaryItemInstance");
			put(82, "SecondaryItemType");
			put(83, "SecondaryItemInstance");
			put(84, "UserType");
			put(85, "UserInstance");
			put(86, "AreaType");
			put(87, "AreaInstance");
			put(88, "DefaultPos");
			put(89, "Breed");
			put(90, "ProjectileAC");
			put(91, "MeleeAC");
			put(92, "EnergyAC");
			put(93, "ChemicalAC");
			put(94, "RadiationAC");
			put(95, "ColdAC");
			put(96, "PoisonAC");
			put(97, "FireAC");
			put(98, "StateAction");
			put(99, "ItemAnim");
			put(100, "MartialArts");
			put(101, "MultiMelee");
			put(102, "1hBlunt");
			put(103, "1hEdged");
			put(104, "MeleeEnergy");
			put(105, "2hEdged");
			put(106, "Piercing");
			put(107, "2hBlunt");
			put(108, "SharpObjects");
			put(109, "Grenade");
			put(110, "HeavyWeapons");
			put(111, "Bow");
			put(112, "Pistol");
			put(113, "Rifle");
			put(114, "MG/SMG");
			put(115, "Shotgun");
			put(116, "AssaultRifle");
			put(117, "VehicleWater");
			put(118, "MeleeInit");
			put(119, "RangedInit");
			put(120, "PhysicalInit");
			put(121, "BowSpecialAttack");
			put(122, "SensoryImprovement");
			put(123, "FirstAid");
			put(124, "Treatment");
			put(125, "MechanicalEngineering");
			put(126, "ElectricalEngineering");
			put(127, "MaterialMetamorphose");
			put(128, "BiologicalMetamorphose");
			put(129, "PsychologicalModification");
			put(130, "MaterialCreation");
			put(131, "SpaceTime");
			put(132, "NanoPool");
			put(133, "RangedEnergy");
			put(134, "MultiRanged");
			put(135, "TrapDisarm");
			put(136, "Perception");
			put(137, "Adventuring");
			put(138, "Swimming");
			put(139, "VehicleAir");
			put(140, "MapNavigation");
			put(141, "Tutoring");
			put(142, "Brawl");
			put(143, "Riposte");
			put(144, "Dimach");
			put(145, "Parry");
			put(146, "SneakAttack");
			put(147, "FastAttack");
			put(148, "Burst");
			put(149, "NanoInit");
			put(150, "FlingShot");
			put(151, "AimedShot");
			put(152, "BodyDevelopment");
			put(153, "DuckExplosions");
			put(154, "DodgeRanged");
			put(155, "EvadeClose");
			put(156, "RunSpeed");
			put(157, "QuantumFT");
			put(158, "WeaponSmithing");
			put(159, "Pharmaceuticals");
			put(160, "NanoProgramming");
			put(161, "ComputerLiteracy");
			put(162, "Psychology");
			put(163, "Chemistry");
			put(164, "Concealment");
			put(165, "BreakingEntry");
			put(166, "VehicleGround");
			put(167, "FullAuto");
			put(168, "NanoResist");
			put(169, "AlienLevel");
			put(170, "HealthChangeBest");
			put(171, "HealthChangeWorst");
			put(172, "HealthChange");
			put(173, "CurrentMovementMode");
			put(174, "PrevMovementMode");
			put(175, "AutoLockTimeDefault");
			put(176, "AutoUnlockTimeDefault");
			put(177, "MoreFlags");
			put(178, "AlienNextXP");
			put(179, "NPCFlags");
			put(180, "CurrentNCU");
			put(181, "MaxNCU");
			put(182, "Specialization");
			put(183, "EffectIcon");
			put(184, "BuildingType");
			put(185, "BuildingInstance");
			put(186, "CardOwnerType");
			put(187, "CardOwnerInstance");
			put(188, "BuildingComplexInst");
			put(189, "ExitInstance");
			put(190, "NextDoorInBuilding");
			put(191, "LastConcretePlayfieldInstance");
			put(192, "ExtenalPlayfieldInstance");
			put(193, "ExtenalDoorInstance");
			put(194, "InPlay");
			put(195, "AccessKey");
			put(196, "ConflictReputation");
			put(197, "OrientationMode");
			put(198, "SessionTime");
			put(199, "RP");
			put(200, "Conformity");
			put(201, "Aggressiveness");
			put(202, "Stability");
			put(203, "Extroverty");
			put(204, "Taunt");
			put(205, "ReflectProjectileAC");
			put(206, "ReflectMeleeAC");
			put(207, "ReflectEnergyAC");
			put(208, "ReflectChemicalAC");
			put(209, "WeaponMesh");
			put(210, "RechargeDelay");
			put(211, "EquipDelay");
			put(212, "MaxEnergy");
			put(213, "TeamFaction");
			put(214, "CurrentNano");
			put(215, "GmLevel");
			put(216, "ReflectRadiationAC");
			put(217, "ReflectColdAC");
			put(218, "ReflectNanoAC");
			put(219, "ReflectFireAC");
			put(220, "CurrBodyLocation");
			put(221, "MaxNanoEnergy");
			put(222, "AccumulatedDamage");
			put(223, "CanChangeClothes");
			put(224, "Features");
			put(225, "ReflectPoisonAC");
			put(226, "ShieldProjectileAC");
			put(227, "ShieldMeleeAC");
			put(228, "ShieldEnergyAC");
			put(229, "ShieldChemicalAC");
			put(230, "ShieldRadiationAC");
			put(231, "ShieldColdAC");
			put(232, "ShieldNanoAC");
			put(233, "ShieldFireAC");
			put(234, "ShieldPoisonAC");
			put(235, "BerserkMode");
			put(236, "InsurancePercentage");
			put(237, "ChangeSideCount");
			put(238, "AbsorbProjectileAC");
			put(239, "AbsorbMeleeAC");
			put(240, "AbsorbEnergyAC");
			put(241, "AbsorbChemicalAC");
			put(242, "AbsorbRadiationAC");
			put(243, "AbsorbColdAC");
			put(244, "AbsorbFireAC");
			put(245, "AbsorbPoisonAC");
			put(246, "AbsorbNanoAC");
			put(247, "TemporarySkillReduction");
			put(248, "BirthDate");
			put(249, "LastSaved");
			put(250, "SoundVolume");
			put(251, "CheckPetType");
			put(252, "MetersWalked");
			put(253, "QuestLevelsSolved");
			put(254, "MonsterLevelsKilled");
			put(255, "PvPLevelsKilled");
			put(256, "MissionBitsA");
			put(257, "MissionBitsB");
			put(258, "AccessGrant");
			put(259, "DoorFlags");
			put(260, "ClanHierarchy");
			put(261, "QuestStat");
			put(262, "ClientActivated");
			put(263, "Brawl1Weapon");
			put(264, "Brawl2Weapon");
			put(265, "DimachWeapon");
			put(266, "MartialArtsWeapon");
			put(267, "RiposteWeapon");
			put(263, "PersonalResearchLevel");
			put(264, "GlobalResearchLevel");
			put(265, "PersonalResearchGoal");
			put(266, "GlobalResearchGoal");
			put(267, "TurnSpeed");
			put(268, "LiquidType");
			put(269, "GatherSound");
			put(270, "CastSound");
			put(271, "TravelSound");
			put(272, "HitSound");
			put(273, "SecondaryItemTemplate");
			put(274, "EquippedWeapons");
			put(275, "XPKillRange");
			put(276, "AddAllOffense");
			put(277, "AddAllDefense");
			put(278, "ProjectileDamageModifier");
			put(279, "MeleeDamageModifier");
			put(280, "EnergyDamageModifier");
			put(281, "ChemicalDamageModifier");
			put(282, "RadiationDamageModifier");
			put(283, "ItemHateValue");
			put(284, "CriticalBonus");
			put(285, "MaxDamage");
			put(286, "MinDamage");
			put(287, "AttackRange");
			put(288, "HateValueModifier");
			put(289, "TrapDifficulty");
			put(290, "StatOne");
			put(291, "NumAttackEffects");
			put(292, "DefaultAttackType");
			put(293, "ItemSkill");
			put(294, "AttackDelay");
			put(295, "ItemOpposedSkill");
			put(296, "ItemSIS");
			put(297, "InteractionRadius");
			put(298, "Slot");
			put(299, "LockDifficulty");
			put(300, "Members");
			put(301, "MinMembers");
			put(302, "ClanPrice");
			put(303, "ClanUpkeep");
			put(304, "ClanType");
			put(305, "ClanInstance");
			put(306, "VoteCount");
			put(307, "MemberType");
			put(308, "MemberInstance");
			put(309, "GlobalClanType");
			put(310, "GlobalClanInstance");
			put(311, "ColdDamageModifier");
			put(312, "ClanUpkeepInterval");
			put(313, "TimeSinceUpkeep");
			put(314, "ClanFinalized");
			put(315, "NanoDamageModifier");
			put(316, "FireDamageModifier");
			put(317, "PoisonDamageModifier");
			put(318, "NanoCost");
			put(319, "XPModifier");
			put(320, "BreedLimit");
			put(321, "GenderLimit");
			put(322, "LevelLimit");
			put(323, "PlayerKilling");
			put(324, "TeamAllowed");
			put(325, "WeaponDisallowedType");
			put(326, "WeaponDisallowedInstance");
			put(327, "Taboo");
			put(328, "Compulsion");
			put(329, "SkillDisabled");
			put(330, "ClanItemType");
			put(331, "ClanItemInstance");
			put(332, "DebuffFormula");
			put(333, "PvPRating");
			put(334, "SavedXP");
			put(335, "DoorBlockTime");
			put(336, "OverrideTexture");
			put(337, "OverrideMaterial");
			put(338, "DeathReason");
			put(339, "DamageType");
			put(340, "BrainType");
			put(341, "XPBonus");
			put(342, "HealInterval");
			put(343, "HealDelta");
			put(344, "MonsterTexture");
			put(345, "HasAlwaysLootable");
			put(346, "TradeLimit");
			put(347, "FaceTexture");
			put(348, "SpecialCondition");
			put(349, "AutoAttackFlags");
			put(350, "NextXP");
			put(351, "TeleportPauseMilliSeconds");
			put(352, "SISCap");
			put(353, "AnimSet");
			put(354, "AttackType");
			put(355, "WornItem");
			put(356, "NPCHash");
			put(357, "CollisionRadius");
			put(358, "OuterRadius");
			put(359, "ShapeShift");
			put(360, "Scale");
			put(361, "HitEffectType");
			put(362, "ResurrectDestination");
			put(363, "NanoInterval");
			put(364, "NanoDelta");
			put(365, "ReclaimItem");
			put(366, "GatherEffectType");
			put(367, "VisualBreed");
			put(368, "VisualProfession");
			put(369, "VisualGender");
			put(370, "RitualTargetInst");
			put(371, "SkillTimeOnSelectedTarget");
			put(372, "LastSaveXP");
			put(373, "ExtendedTime");
			put(374, "BurstRecharge");
			put(375, "FullAutoRecharge");
			put(376, "GatherAbstractAnim");
			put(377, "CastTargetAbstractAnim");
			put(378, "CastSelfAbstractAnim");
			put(379, "CriticalIncrease");
			put(380, "WeaponRange");
			put(381, "NanoRange");
			put(382, "SkillLockModifier");
			put(383, "NanoInterruptModifier");
			put(384, "EntranceStyles");
			put(385, "ChanceOfBreakOnNanoAttack");
			put(386, "ChanceOfBreakOnDebuff");
			put(387, "DieAnim");
			put(388, "TowerType");
			put(389, "Expansion");
			put(390, "LowresMesh");
			put(391, "CriticalResistance");
			put(392, "OldTimeExist");
			put(393, "ResistModifier");
			put(394, "ChestFlags");
			put(395, "PrimaryTemplateID");
			put(396, "NumberOfItems");
			put(397, "SelectedTargetType");
			put(398, "CorpseHash");
			put(399, "AmmoName");
			put(400, "Rotation");
			put(401, "CATAnim");
			put(402, "CATAnimFlags");
			put(403, "DisplayCATAnim");
			put(404, "DisplayCATMesh");
			put(405, "NanoSchool");
			put(406, "NanoSpeed");
			put(407, "NanoPoints");
			put(408, "TrainSkill");
			put(409, "TrainSkillCost");
			put(410, "InFight");
			put(411, "NextFormula");
			put(412, "MultipleCount");
			put(413, "EffectType");
			put(414, "ImpactEffectType");
			put(415, "CorpseType");
			put(416, "CorpseInstance");
			put(417, "CorpseAnimKey");
			put(418, "UnarmedTemplateInstance");
			put(419, "TracerEffectType");
			put(420, "AmmoType");
			put(421, "CharRadius");
			put(422, "ChanceOfUse");
			put(423, "CurrentState");
			put(424, "ArmorType");
			put(425, "RestModifier");
			put(426, "BuyModifier");
			put(427, "SellModifier");
			put(428, "CastEffectType");
			put(429, "NPCBrainState");
			put(430, "WaitState");
			put(431, "SelectedTarget");
			put(432, "ErrorCode");
			put(433, "OwnerInstance");
			put(434, "CharState");
			put(435, "ReadOnly");
			put(436, "DamageType");
			put(437, "CollideCheckInterval");
			put(438, "PlayfieldType");
			put(439, "NPCCommand");
			put(440, "InitiativeType");
			put(441, "CharTmp1");
			put(442, "CharTmp2");
			put(443, "CharTmp3");
			put(444, "CharTmp4");
			put(445, "NPCCommandArg");
			put(446, "NameTemplate");
			put(447, "DesiredTargetDistance");
			put(448, "VicinityRange");
			put(449, "NPCIsSurrendering");
			put(450, "StateMachine");
			put(451, "NPCSurrenderInstance");
			put(452, "NPCHasPatrolList");
			put(453, "NPCVicinityChars");
			put(454, "ProximityRangeOutdoors");
			put(455, "NPCFamily");
			put(456, "CommandRange");
			put(457, "NPCHatelistSize");
			put(458, "NPCNumPets");
			put(459, "ODMinSizeAdd");
			put(460, "EffectRed");
			put(461, "EffectGreen");
			put(462, "EffectBlue");
			put(463, "ODMaxSizeAdd");
			put(464, "DurationModifier");
			put(465, "NPCCryForHelpRange");
			put(466, "LOSHeight");
			put(467, "PetReq1");
			put(467, "SLZoneProtection");
			put(468, "PetReq2");
			put(469, "PetReq3");
			put(470, "MapUpgrades");
			put(471, "MapFlags1");
			put(472, "MapFlags2");
			put(473, "FixtureFlags");
			put(474, "FallDamage");
			put(475, "MaxReflectedProjectileAC");
			put(476, "MaxReflectedMeleeAC");
			put(477, "MaxReflectedEnergyAC");
			put(478, "MaxReflectedChemicalAC");
			put(479, "MaxReflectedRadiationAC");
			put(480, "MaxReflectedColdAC");
			put(481, "MaxReflectedNanoAC");
			put(482, "MaxReflectedFireAC");
			put(483, "MaxReflectedPoisonAC");
			put(484, "ProximityRangeIndoors");
			put(485, "PetReqVal1");
			put(486, "PetReqVal2");
			put(487, "PetReqVal3");
			put(488, "TargetFacing");
			put(489, "Backstab");
			put(490, "OriginatorType");
			put(491, "QuestInstance");
			put(492, "QuestIndex1");
			put(493, "QuestIndex2");
			put(494, "QuestIndex3");
			put(495, "QuestIndex4");
			put(496, "QuestIndex5");
			put(497, "QTDungeonInstance");
			put(498, "QTNumMonsters");
			put(499, "QTKilledMonsters");
			put(500, "AnimPos");
			put(501, "AnimPlay");
			put(502, "AnimSpeed");
			put(503, "QTKillNumMonsterID1");
			put(504, "QTKillNumMonsterCount1");
			put(505, "QTKillNumMonsterID2");
			put(506, "QTKillNumMonsterCount2");
			put(507, "QTKillNumMonsterID3");
			put(508, "QTKillNumMonsterCount3");
			put(509, "QuestIndex0");
			put(510, "QuestTimeout");
			put(511, "TowerNPCHash");
			put(512, "PetType");
			put(513, "OnTowerCreation");
			put(514, "OwnedTowers");
			put(515, "TowerInstance");
			put(516, "AttackShield");
			put(517, "SpecialAttackShield");
			put(518, "NPCVicinityPlayers");
			put(519, "NPCUseFightModeRegenRate");
			put(520, "RandomNumberRoll");
			put(521, "SocialStatus");
			put(522, "LastRnd");
			put(523, "AttackDelayCap");
			put(524, "RechargeDelayCap");
			put(525, "RemainingHealth");
			put(526, "RemainingNano");
			put(527, "TargetDistance");
			put(528, "TeamLevel");
			put(529, "NumberOnHateList");
			put(530, "ConditionState");
			put(531, "ExpansionPlayfield");
			put(532, "ShadowBreed");
			put(533, "NPCFovStatus");
			put(534, "DudChance");
			put(535, "HealModifier");
			put(536, "NanoDamage");
			put(537, "NanoVulnerability");
			put(538, "AMSCap");
			put(539, "ProcInitiative1");
			put(540, "ProcInitiative2");
			put(541, "ProcInitiative3");
			put(542, "ProcInitiative4");
			put(543, "FactionModifier");
			put(546, "StackingLine2");
			put(547, "StackingLine3");
			put(548, "StackingLine4");
			put(549, "StackingLine5");
			put(550, "StackingLine6");
			put(551, "StackingOrder");
			put(552, "ProcNano1");
			put(553, "ProcNano2");
			put(554, "ProcNano3");
			put(555, "ProcNano4");
			put(556, "ProcChance1");
			put(557, "ProcChance2");
			put(558, "ProcChance3");
			put(559, "ProcChance4");
			put(560, "OTArmedForces");
			put(561, "ClanSentinels");
			put(562, "OTMed");
			put(563, "ClanGaia");
			put(564, "OTTrans");
			put(565, "ClanVanguards");
			put(566, "GaurdianOfShadows");
			put(567, "OTFollowers");
			put(568, "OTOperator");
			put(569, "OTUnredeemed");
			put(570, "ClanDevoted");
			put(571, "ClanConserver");
			put(572, "ClanRedeemed");
			put(573, "SK");
			put(574, "LastSK");
			put(575, "NextSK");
			put(576, "PlayerOptions");
			put(577, "LastPerkResetTime");
			put(578, "CurrentTime");
			put(579, "ShadowBreedTemplate");
			put(580, "NPCVicinityFamily");
			put(581, "NPCScriptAMSScale");
			put(582, "ApartmentsAllowed");
			put(583, "ApartmentsOwned");
			put(584, "ApartmentAccessCard");
			put(585, "MapFlags3");
			put(586, "MapFlags4");
			put(587, "NumberOfTeamMembers");
			put(588, "ActionCategory");
			put(589, "CurrentPlayfield");
			put(590, "DistrictNano");
			put(591, "DistrictNanoInterval");
			put(592, "UnsavedXP");
			put(593, "RegainXP");
			put(594, "TempSaveTeamID");
			put(595, "TempSavePlayfield");
			put(596, "TempSaveX");
			put(597, "TempSaveY");
			put(598, "ExtendedFlags");
			put(599, "ShopPrice");
			put(600, "NewbieHP");
			put(601, "HPLevelUp");
			put(602, "HPPerSkill");
			put(603, "NewbieNP");
			put(604, "NPLevelUp");
			put(605, "NPPerSkill");
			put(606, "MaxShopItems");
			put(607, "PlayerID");
			put(608, "ShopRent");
			put(609, "SynergyHash");
			put(610, "ShopFlags");
			put(611, "ShopLastUsed");
			put(612, "ShopType");
			put(613, "LockDownTime");
			put(614, "LeaderLockDownTime");
			put(615, "InvadersKilled");
			put(616, "KilledByInvaders");
			put(620, "HouseTemplate");
			put(621, "FireDamage");
			put(622, "ColdDamage");
			put(623, "MeleeDamage");
			put(624, "ProjectileDamage");
			put(625, "PoisonDamage");
			put(626, "RadiationDamage");
			put(627, "EnergyDamage");
			put(628, "ChemicalDamage");
			put(629, "TotalDamage");
			put(630, "TrackProjectileDamage");
			put(631, "TrackMeleeDamage");
			put(632, "TrackEnergyDamage");
			put(633, "TrackChemicalDamage");
			put(634, "TrackRadiationDamage");
			put(635, "TrackColdDamage");
			put(636, "TrackPoisonDamage");
			put(637, "TrackFireDamage");
			put(638, "NPCSpellArg1");
			put(639, "NPCSpellRet1");
			put(640, "CityInstance");
			put(641, "DistanceToSpawnpoint");
			put(642, "CityTerminalRechargePercent");
			put(651, "AdvantageHash1");
			put(652, "AdvantageHash2");
			put(653, "AdvantageHash3");
			put(654, "AdvantageHash4");
			put(655, "AdvantageHash5");
			put(656, "ShopIndex");
			put(657, "ShopID");
			put(658, "IsVehicle");
			put(659, "DamageToNano");
			put(660, "AccountFlags");
			put(661, "DamageToNano");
			put(662, "MechData");
			put(663, "PointValue");
			put(664, "VehicleAC");
			put(665, "VehicleDamage");
			put(666, "VehicleHealth");
			put(667, "VehicleSpeed");
			put(668, "BattlestationFaction");
			put(669, "VP");
			put(670, "BattlestationRep");
			put(671, "PetState");
			put(672, "PaidPoints");
			put(700, "ItemSeed");
			put(701, "ItemLevel");
			put(702, "ItemTemplateID");
			put(703, "ItemTemplateID2");
			put(704, "ItemCategoryID");
			put(768, "HasKnubotData");
			put(800, "QuestBoothDifficulty");
			put(801, "QuestASMinimumRange");
			put(802, "QuestASMaximumRange");
			put(888, "VisualLODLevel");
			put(889, "TargetDistanceChange");
			put(900, "TideRequiredDynelID");
			put(999, "StreamCheckMagic");
			put(1001, "Type");
			put(1002, "Instance");
			put(62, "ClanTokens");
			put(75, "OmniTokens");
		}
	};
	
	public static String getNanoStrain(int id) {
		if (nanoStrainList.containsKey(id)) {
			return nanoStrainList.get(id);
		} else {
			return String.valueOf(id);
		}		
	}
	
	public static String getSkill(int id) {
		if (skillList.containsKey(id)) {
			return skillList.get(id);
		} else {
			return String.valueOf(id);
		}
	}
	
	public static String getFlag(int id) {
		if (flagList.containsKey(id)) {
			return flagList.get(id);
		} else {
			return String.valueOf(id);
		}
	}
	
	public static String getExpansion(int id) {
		if (expansionList.containsKey(id)) {
			return expansionList.get(id);
		} else {
			return String.valueOf(id);
		}
	}
	
	public static String getCurrentPlayfield(int id) {
		if (currentPlayfieldList.containsKey(id)) {
			return currentPlayfieldList.get(id);
		} else {
			return String.valueOf(id);
		}
	}

	public static String padLeft(String s, int n) {
	    return String.format("%1$#" + n + "s", s).replaceAll(" ", "0");  
	}
	
	public static String getSlot(int id, int page) {
		String binary = new StringBuffer(String.format("%16s", Integer.toBinaryString(id)).replace(' ', '0')).reverse().toString();
		String slots = "";
		
		if (page == 2 || page == 3) {
			binary = binary.substring(1);
		}
		
		char[] ids = binary.toCharArray();
		
		Logging.log(APP_TAG, "Slots: " + binary + ", on page: " + page);
		int slotCounter = 0;
		
		if (page == 1) {
			for (char i : ids) {
				Logging.log(APP_TAG, "Val: " + i + ", counter: " + slotCounter);
				if (MathUtils.stringToBool(Character.toString(i))) {
					if (slotWeaponsList.get(slotCounter) != null) {
						if (slots.length() > 0) {
							slots += ", ";
						}
						slots += slotWeaponsList.get(slotCounter);
					}
				}
				slotCounter++;
			}
		} else if (page == 2) {
			for (char i : ids) {
				Logging.log(APP_TAG, "Val: " + i + ", counter: " + slotCounter);
				if (MathUtils.stringToBool(Character.toString(i))) {
					if (slotClothingList.get(slotCounter + 1) != null) {
						if (slots.length() > 0) {
							slots += ", ";
						}
						slots += slotClothingList.get(slotCounter + 1);
					}
				}
				slotCounter++;
			}
		} else if (page == 3) {
			for (char i : ids) {
				Logging.log(APP_TAG, "Val: " + i + ", counter: " + slotCounter);
				if (MathUtils.stringToBool(Character.toString(i))) {
					if (slotImplantsList.get(slotCounter + 1) != null) {
						if (slots.length() > 0) {
							slots += ", ";
						}
						slots += slotImplantsList.get(slotCounter + 1);
					}
				}
				slotCounter++;
			}
		}
		
		return slots;
	}
	
	public static String getFaction(int id) {
		if (factionList.containsKey(id)) {
			return factionList.get(id);
		} else {
			return String.valueOf(id);
		}
	}
	
	public static String getBreed(int id) {
		if (breedList.containsKey(id)) {
			return breedList.get(id);
		} else {
			return String.valueOf(id);
		}
	}
	
	public static String getProfession(int id) {
		if (professionList.containsKey(id)) {
			return professionList.get(id);
		} else {
			return String.valueOf(id);
		}
	}
	
	public static String getPerk(int id) {
		if (perkList.containsKey(id)) {
			return perkList.get(id);
		} else {
			return String.valueOf(id);
		}
	}
	
	public static String getExpansionPlayfield(int id) {
		if (expansionPlayfieldList.containsKey(id)) {
			return expansionPlayfieldList.get(id);
		} else {
			return String.valueOf(id);
		}
	}

	public static String getWornItem(int id) {
		if (wornItemList.containsKey(id)) {
			return wornItemList.get(id);
		} else {
			return String.valueOf(id);
		}
	}
	
	public static String getHasNotWornItem(int id) {
		return lookupItemName(id);
		/*
		if (hasNotWornItemList.containsKey(id)) {
			return hasNotWornItemList.get(id);
		} else {
			return String.valueOf(id);
		}
		*/
	}

	public static String lookupItemName(int id) {
    	String xml = null;
        Document doc = null;
        
        String name = "";

        try {
            DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(String.format(Statics.XYPHOS_ITEM_URL, id));
            HttpResponse httpResponse = httpClient.execute(httpPost);
            HttpEntity httpEntity = httpResponse.getEntity();
            xml = EntityUtils.toString(httpEntity);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        if (xml != null) {
            Pattern pattern = Pattern.compile("<description>(.*?)</description>");
            Matcher matcher = pattern.matcher(xml);
            
	        while(matcher.find()) {
	        	xml = xml.replace(matcher.group(1), matcher.group(1).replace("<", "&lt;").replace(">", "&gt;"));
	        }	        
        	
	        pattern = Pattern.compile("name=\"(.*?)\"");
	        
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            matcher = pattern.matcher(xml);
            
	        while(matcher.find()) {
	        	xml = xml.replace(matcher.group(1), matcher.group(1).replace("<b>", "").replace("</b>", ""));
	        }	        
            
            try {
                DocumentBuilder db = dbf.newDocumentBuilder();
     
                InputSource is = new InputSource();
                is.setCharacterStream(new StringReader(xml));
                doc = db.parse(is); 
            } catch (ParserConfigurationException e) {
                Log.e("Error: ", e.getMessage());
                return null;
            } catch (SAXException e) {
                Log.e("Error: ", e.getMessage());
                return null;
            } catch (IOException e) {
                Log.e("Error: ", e.getMessage());
                return null;
            }
        }
        
        if (doc != null) {
        	NodeList nl = doc.getElementsByTagName("item");
            
            for (int i = 0; i < nl.getLength(); i++) {
                Element e = (Element) nl.item(i);
                name = getValue(e, "name");
            }
        }
        
        if (name.equals("")) {
        	return String.valueOf(id);
        } else {
        	return "<a href=\"itemref://" + id + "/0/0\">" + name + "</a>";
        }
	}
	
    private static String getValue(Element item, String str) {
    	NodeList n = item.getElementsByTagName(str);
    	return getElementValue(n.item(0));
    }

    private static final String getElementValue( Node elem ) {
    	Node child;

    	if (elem != null) {
    		if (elem.hasChildNodes()) {
    			for (child = elem.getFirstChild(); child != null; child = child.getNextSibling()) {
    				if (child.getNodeType() == Node.TEXT_NODE) {
    					return child.getNodeValue();
    				}
    			}
    		}
    	}

    	return "";
    }
    
    public static String getNano(int id) {
        HttpClient client = new DefaultHttpClient();
        HttpConnectionParams.setConnectionTimeout(client.getParams(), 0);
        HttpResponse response = null;
        
        JSONObject json = new JSONObject();
        
        try {
            /**
             * TODO
             */
        	HttpPost post = new HttpPost(String.format("http://109.74.0.178/nano.php?id=%d", id));
            post.setHeader("Content-Type", "application/json; charset=utf-8");
            
            json.put("platformType", "android");
			
            StringEntity se = new StringEntity(json.toString());  
            
            post.setEntity(se);
            response = client.execute(post);
        } catch(Exception e){
            e.printStackTrace();
        }
        
        if (response != null) {
            InputStream in;
            
			try {
				in = response.getEntity().getContent();
				String result = ServiceTools.convertStreamToString(in);
				                
    			if (result != null) {
    				JSONObject jsondata = new JSONObject(result);
                    
                    if(jsondata != null) {
                    	if (!jsondata.isNull("name")) {
                    		return jsondata.getString("name");
                    	}
                    }
    			}
			} catch (IllegalStateException e) {
	            e.printStackTrace();
			} catch (IOException e) {
	            e.printStackTrace();
			} catch (JSONException e) {
	            e.printStackTrace();
			}
        }
        
    	return String.valueOf(id);
    }
}
