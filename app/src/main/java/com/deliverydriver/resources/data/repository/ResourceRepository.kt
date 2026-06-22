package com.deliverydriver.resources.data.repository

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import com.deliverydriver.resources.data.models.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object ResourceRepository {

    // ── Categories with detailed resources ──────────────────────────
    val categories = listOf(
        ResourceCategory(
            id = "loadout",
            title = "Loadout & Organizing",
            icon = Icons.Filled.Inventory2,
            description = "Master your loadout process and package organization",
            items = listOf(
                ResourceItem("load1", "Loadout Checklist",
                    "Follow these steps every shift to stay organized.",
                    """• Arrive 15 min early — check in at the dispatch desk
• Verify your route size: # of packages, stops, overflow
• Scan all totes/bags before loading
• Organize overflow by driver aid number (yellow sticker)
• Load totes in reverse stop order (last stop first)
• Place overflow sorted ascending left-to-right
• Keep a sharpie, extra charger, water, snacks handy
• Take a photo of your route manifest
• Check van condition — tires, lights, fluids, cleanliness
• Set up your phone mount and aux/USB before driving"""),
                ResourceItem("load2", "Package Sorting System",
                    "The yellow sticker is your best friend — here's how to read it.",
                    """Driver Aid Number = your sorting key
• 3-digit number on the yellow label
• Sort totes from highest to lowest (load last-first)
• Within each tote, sort packages by D.A. number ascending
• Overflow boxes: place in ascending order in the van

Pro tip: Take 5 extra minutes at loadout to sort well —
it saves 30+ minutes on route looking for packages."""),
                ResourceItem("load3", "Overflow Organization",
                    "Big boxes need special treatment to stay accessible.",
                    """• Always load overflow with labels facing YOU (the sliding door)
• Group by driver aid number range: 1-100, 101-200, etc.
• Heavy boxes on bottom, light on top
• Don't stack higher than window line — you need visibility
• Leave a walking path down the middle if possible
• Use bungee cords to secure tall stacks
• Take a quick photo of your organized van — helps if something shifts"""),
                ResourceItem("load4", "Phone & Tech Setup",
                    "Get your devices ready before you pull out.",
                    """• Both phones fully charged (bring backup battery)
• Flex app open, route loaded, Bluetooth ON
• Waze or Google Maps running on 2nd device for navigation
• Enable 'Do Not Disturb' while driving
• Download offline maps for your route area (rural = spotty signal)
• Test that your Bluetooth earpiece works for calling customers
• Keep USB cables accessible"""),
                ResourceItem("load5", "Morning Prep Routine",
                    "What to do before your first delivery.",
                    """• Hydrate — start drinking water immediately
• Eat a solid breakfast with protein
• Stretch — especially back, shoulders, and neck
• Confirm your first 5 stops in the Flex app
• Check for any notes on those first stops (gate codes, dogs, etc.)
• Set your delivery area offline maps just in case
• Text dispatch that you're about to leave the station""")
            )
        ),
        ResourceCategory(
            id = "delivery",
            title = "Delivery Tips & Tricks",
            icon = Icons.Filled.TouchApp,
            description = "Best practices for smooth deliveries",
            items = listOf(
                ResourceItem("del1", "Approaching the Stop",
                    "How to handle each stop efficiently and safely.",
                    """• Park facing the direction of your next stop (save time!)
• Put the van in park, set parking brake every time
• Grab packages for this stop AND the next stop (2-stop method)
• Scan and check customer notes BEFORE walking up
• Look for delivery instructions in the app
• Approach the door with package in hand, phone ready
• Take photo quickly but make sure it's clear
• Mark delivered, confirm, and move to next

Pro tip: Holding 2 stops at once saves 10-15 minutes daily."""),
                ResourceItem("del2", "Apartments & Gated Communities",
                    "Apartments can be time sinks. Here's how to handle them.",
                    """• Check for access codes in customer notes FIRST
• If no code, try: #0000, #1234, [#], or call box
• For call boxes: try every tenant number listed (on-site)
• If locked out for more than 2 min, mark as 'unable to deliver'
• Time-saving: group all apartment stops and do them in one pass
• Note access codes for future reference (use the code storage in this app!)
• For large complexes: park near the leasing office and ask for a map
• Business deliveries: deliver before 5 PM or they might be closed"""),
                ResourceItem("del3", "Rural & Country Routes",
                    "Long driveways, dirt roads, and farm animals.",
                    """• Slow down — rural roads are unpredictable
• Watch for: loose dogs, farm equipment, blind driveways
• If a driveway looks too long or rough, leave at the gate/mailbox
• Turn around carefully — avoid backing into ditches
• GPS may be wrong in rural areas — use offline maps
• Carry a flashlight for dark driveways
• Watch for chains across driveways (don't drive past them!)
• If you see 'no trespassing' signs, leave at the road"""),
                ResourceItem("del4", "Night Delivery Strategies",
                    "Delivering after dark requires extra caution.",
                    """• Use your phone flashlight at every stop
• Wear reflective vest if you have one
• Approach carefully — watch for steps, uneven ground, pets
• Take extra care with photos — make sure address is visible
• If area feels unsafe, call dispatch — trust your gut
• Park under street lights when possible
• Lock van doors immediately after getting out
• Keep your head on a swivel in high-crime areas"""),
                ResourceItem("del5", "Business Deliveries",
                    "Delivering to stores and offices during work hours.",
                    """• Best window: 9 AM - 4 PM (before businesses close)
• If closed: check for a receiving dock or back door
• Leave with a neighbor or security desk if possible
• Note business hours for future deliveries
• Some businesses have specific delivery instructions — check notes
• Office buildings: ask front desk where to leave packages
• Restaurants: check for a back door or delivery shelf"""),
                ResourceItem("del6", "Weather & Difficult Conditions",
                    "Rain, snow, heat — keep going safely.",
                    """• Rain: use plastic bags for packages if needed, watch for slippery steps
• Heat: stay hydrated, take breaks, park in shade, wear sunscreen
• Cold: layer up, keep gloves that work with your phone (capacitive)
• Snow/ice: walk slowly, wear boots with grip, don't rush
• High wind: watch for falling branches, secure van doors
• Always keep a bottle of water in the van
• Take your lunch break in a shaded or air-conditioned spot"""),
                ResourceItem("del7", "The One-Photo Rule",
                    "Photos are critical — here's how to do them right.",
                    """• Photo must show the package at the delivery location
• Include the house number or a recognizable landmark
• Make sure photo is well-lit — use flash at night
• For apartments: take photo showing unit number if possible
• If photo fails: retake before marking delivered
• Bad photos = DNRs (did not receive) which hurt your score
• Keep it quick but clear — don't spend 30 seconds on it""")
            )
        ),
        ResourceCategory(
            id = "safety",
            title = "Safety & Emergency",
            icon = Icons.Filled.Shield,
            description = "Stay safe out there",
            items = listOf(
                ResourceItem("saf1", "Dog Safety Protocol",
                    "Dogs are the #1 hazard. Know how to handle them.",
                    """• Always check customer notes for dog warnings
• Listen for barking before opening the gate
• Carry dog treats (optional, but can help)
• If a dog approaches aggressively: STOP, don't run
• Face the dog, stand still, speak firmly 'go home'
• Use the package as a shield between you and the dog
• Back away slowly — never turn your back
• If trapped: get on top of a car/porch and call 911
• Report any aggressive dog in the Flex app
• You have the right to skip a stop if you feel unsafe"""),
                ResourceItem("saf2", "Vehicle Emergencies",
                    "Van broke down? Tire flat? Here's what to do.",
                    """• Flat tire: pull over safely, turn on hazards, call dispatch
• DON'T try to change the tire yourself unless trained
• Engine trouble: turn off van, call dispatch immediately
• Accident (even minor): call dispatch first, then police if needed
• Take photos of any damage before moving vehicles
• Exchange insurance info only through dispatch
• If van is undrivable: stay with the van, dispatch will send help
• Dead battery: call for roadside assistance through dispatch
• Always keep emergency numbers on your phone (not just in the app)"""),
                ResourceItem("saf3", "Personal Safety Tips",
                    "Look out for yourself on every shift.",
                    """• Trust your gut — if a stop feels sketchy, skip it
• Park in well-lit areas whenever possible
• Lock van doors every time you get out (even for 10 seconds)
• Don't wear headphones/earbuds that block ambient sound
• Keep your phone charged and accessible
• Share your location with a family member/friend
• Carry pepper spray if allowed in your area
• Know where the nearest hospital/police station is on your route
• Stay aware of your surroundings, not just your phone screen
• If approached aggressively, get back in the van, lock doors, call 911"""),
                ResourceItem("saf4", "Emergency Contacts",
                    "Keep these numbers saved in your phone.",
                    """• STATION DISPATCH: [Write in your station's number]
• DSP Owner/Manager: [Write in your DSP's number]
• Amazon Delivery Support: 1-877-485-7274 (Emergency)
• Roadside Assistance: [Write in your provider's number]
• Local Police: [Your city's non-emergency number]
• Emergency Services: 911
• Workers Comp / HR: [Your DSP's HR number]
• Personal Emergency Contact: [Family/friend number]"""),
                ResourceItem("saf5", "Heat & Dehydration Prevention",
                    "Delivery vans get HOT — take care of yourself.",
                    """• Drink water consistently throughout the day — don't wait until you're thirsty
• Keep a cooler with ice water and electrolyte drinks
• Take your 30-min lunch break in AC (find a fast food place)
• Wear light-colored, breathable clothing
• Use a cooling towel around your neck
• Recognize heat exhaustion signs: dizziness, nausea, headache
• If you feel faint: stop, get AC, drink water, tell dispatch
• Wear a wide-brimmed hat and sunglasses
• Reapply sunscreen every 2 hours"""),
                ResourceItem("saf6", "Van Inspection Checklist",
                    "Quick inspection before you leave the station.",
                    """• Walk around the van — check for new dents/damage
• Tires: check pressure, look for bulges or low tread
• All lights working: headlights, brake lights, turn signals
• Windshield/cameras clean and unobstructed
• Check oil and fluid levels (ask mechanic if unsure)
• Seatbelt works properly
• Emergency kit present: first aid, triangles, fire extinguisher
• Spare tire and jack present
• Clean out any trash/debris from previous driver
• Report ANY issues to dispatch before leaving""")
            )
        ),
        ResourceCategory(
            id = "reference",
            title = "Reference Hub",
            icon = Icons.Filled.Book,
            description = "Acronyms, FAQs, station info & more",
            items = listOf(
                ResourceItem("ref1", "Common Amazon Acronyms",
                    "Learn the lingo to talk like a pro.",
                    """• DSP — Delivery Service Partner (your employer)
• DA — Delivery Associate (you!)
• OT — On Time (delivery within the window)
• OTP — On Time Performance (% delivered on time)
• DNR — Did Not Receive (customer claims package not received — BAD)
• POD — Proof of Delivery (the photo you take)
• CC — Customer Contacted (you tried to reach the customer)
• RTS — Return to Station (packages you couldn't deliver)
• SWA — Station With Airport (delivery station near an airport)
• DS — Delivery Station (warehouse where you pick up)
• FC — Fulfillment Center (where Amazon stores inventory)
• TBA — Tracking Number (starts with TBA...)
• VCR — Vehicle Condition Report (daily van check)
• EOC — End of Cycle (returning to station after route)
• OTG — On The Go (training while on route)
• L3/L4 — Amazon employee levels (L3 = blue badge entry)"""),
                ResourceItem("ref2", "Delivery Status Codes",
                    "What each status means and when to use it.",
                    """• Delivered — Package left at customer's address, photo taken
• Unable to deliver — Can't access, customer not available, unsafe
• Reattempt — Will try again later in the day
• Damaged — Package got damaged in transit
• Missing — Can't find the package in your van
• Customer refused — Customer doesn't want the package
• Business closed — Business delivery when store is shut
• No safe location — No covered/dry place to leave it
• Access problem — Gate code not working, can't get in
• Unable to locate address — GPS was wrong, can't find the house

PRO TIP: Use the correct code every time — it protects you from DNRs."""),
                ResourceItem("ref3", "Frequently Asked Questions",
                    "Quick answers to common first-day questions.",
                    """Q: What if I can't finish my route?
A: Call dispatch ASAP — they'll send help or adjust the route.

Q: How do I handle a missing package?
A: Mark it 'missing' in the app, check your van thoroughly, notify dispatch.

Q: What if a customer complains directly to me?
A: Stay calm, apologize, tell them to contact Amazon customer service (you can't handle refunds/complaints).

Q: Can I deliver to a neighbor if no one's home?
A: Only if customer notes say it's okay — otherwise mark 'no safe location'.

Q: What if the Flex app crashes?
A: Force close, reopen. If still broken, call dispatch. Keep delivering with offline mode if possible.

Q: When do I return to station?
A: When your last package is delivered (or all remaining are marked RTS). Don't go back early unless dispatch says so.

Q: Can I use my own car?
A: Usually no — DSP provides a branded van. Some DSPs allow personal vehicles for certain routes."""),
                ResourceItem("ref4", "Station Types Explained",
                    "Know which kind of station you're at.",
                    """• Delivery Station (DS) — Where you'll pick up packages. Sortation center for last-mile delivery.
• Fulfillment Center (FC) — Giant warehouse where inventory is stored. Some drivers pick up from here.
• Sort Center (SC) — Sorts packages between FCs and DSs. Less common for DAs.
• Delivery Station with Airport (SWA) — Near airports, often faster building layout.
• Amazon Fresh / Whole Foods — Grocery delivery, different process, smaller routes.

Your DSP will tell you which station you report to. Most common = DS."""),
                ResourceItem("ref5", "Pay & Schedule Info",
                    "Understand how you get paid.",
                    """• Most DSPs pay per hour (not per package)
• Some pay a daily rate + bonuses
• Typical routes: 8-10 hours, 200-400 packages
• Overtime: usually after 40 hours/week, or after 10 hours/day (varies by DSP)
• Bonuses: some DSPs give bonuses for perfect scores, no DNRs
• Pay cycle: usually weekly or bi-weekly — confirm with your DSP
• Tips: Amazon doesn't include tips for regular delivery drivers (unlike Flex)
• Raises: typically reviewed at 90 days, 6 months, 1 year"""),
                ResourceItem("ref6", "Scorecard & Performance",
                    "How Amazon tracks your performance.",
                    """Amazon tracks DAs on 5 key metrics:

1. Delivery Completion Rate — % of packages delivered vs. attempted
2. On-Time Performance — % delivered within the delivery window
3. DNR Rate — % of customers saying they didn't receive (goal: <1%)
4. Contact Compliance — % of deliveries where you followed customer notes
5. POD Quality — Photo quality and accuracy

Green = great, Yellow = needs improvement, Red = at risk
Red for multiple weeks can lead to suspension or termination."""),
                ResourceItem("ref7", "Essential Gear Checklist",
                    "What to bring every single day.",
                    """• Fully charged phone + backup battery pack
• USB cables (USB-C + Lightning if you have both)
• Phone mount for the van
• Water bottle (large) + electrolyte packets
• Snacks/protein bars/lunch
• Sunscreen
• Sunglasses
• Hat
• Comfortable, breathable clothes + layers
• Sturdy, closed-toe shoes with grip
• Sharpie (for marking packages)
• Flashlight or headlamp
• Hand sanitizer
• Wet wipes or napkins
• Pen and small notebook (for backup)
• Power bank (10,000 mAh minimum)""")
            )
        ),
        ResourceCategory(
            id = "tools",
            title = "Route Tools",
            icon = Icons.Filled.Handyman,
            description = "Loadout checklists, access code storage, and more",
            items = listOf(
                ResourceItem("tool1", "Loadout Checklist (Interactive)",
                    "Check these off every shift to stay sharp.",
                    """Use the interactive checklist in the Route Tools section to track:
□ Checked van condition (tires, lights, damage)
□ Scanned all totes and packages
□ Organized overflow by driver aid number
□ Loaded totes in reverse stop order
□ Setup phone mount and navigation
□ Downloaded offline maps
□ Have enough water and snacks
□ Battery packs charged
□ Emergency contacts saved
□ First 5 stops reviewed in Flex app
□ Notified dispatch I'm rolling"""),
                ResourceItem("tool2", "Access Code Tracker",
                    "Save gate codes and door codes for quick reference.",
                    """• Store access codes by address
• Add notes (e.g., 'call box #204', 'gate code works after 6PM')
• Codes are saved locally on your phone
• Search by address to find a code fast
• Add new codes as you discover them
• Remove old codes that change

Use the 'Save Code' button in Route Tools to start building your library!"""),
                ResourceItem("tool3", "Station Contacts Log",
                    "Keep all your important numbers in one place.",
                    """• Station phone number
• Dispatch direct line
• DSP owner/manager cell
• Roadside assistance
• Warehouse lead/supervisor
• HR/payroll contact

Pro tip: Save these in your phone contacts AND write them on a card in your wallet.
Phones die. Paper doesn't."""),
                ResourceItem("tool4", "Route Time Tracking",
                    "Track your pace and know if you're on schedule.",
                    """• Average pace: 20-25 packages per hour
• Expected stops per hour: 15-20 (varies by density)
• Apartments: ~8-12 stops/hour
• Residential: ~20-25 stops/hour
• Rural: ~8-12 stops/hour
• If you're falling behind 30+ min, call dispatch
• Lunch and breaks are INCLUDED in your route time
• First week goal: focus on accuracy, speed comes later"""),
                ResourceItem("tool5", "Dealing with Common Issues",
                    "Quick fixes for things that go wrong on route.",
                    """• App frozen: Force close, reopen, check if route is still active
• Can't scan barcode: Type TBA number manually
• Wrong GPS location: Use 'Report a Problem' > 'GPS not accurate'
• Customer calls to change delivery: You can only deliver as instructed in the app
• Package gets wet: Mark as damaged, take photo, return to station
• Can't find address: Use Google Maps/Waze to search, then mark location in Flex
• Restroom break: Fast food places, gas stations, or ask a customer politely
• Need to pee urgently: Pull over somewhere safe, go behind the van door (last resort)""")
            )
        )
    )

    // ── Quick Actions ─────────────────────────────────────────────
    val quickActions = listOf(
        QuickAction("qa_checklist", "Loadout\nChecklist", Icons.Filled.Checklist, 0xFF00A67E),
        QuickAction("qa_safety", "Safety\nGuide", Icons.Filled.Shield, 0xFFFF4444),
        QuickAction("qa_codes", "Access\nCodes", Icons.Filled.Lock, 0xFFFFB800),
        QuickAction("qa_tips", "Daily\nTips", Icons.Filled.TipsAndUpdates, 0xFF00A8E8)
    )

    // ── Daily Tips ────────────────────────────────────────────────
    val dailyTips = listOf(
        DailyTip("Hydrate before you feel thirsty — once you're thirsty, it's already late.", "Wellness"),
        DailyTip("The 2-stop package trick: grab packages for this stop AND the next stop on your way back.", "Efficiency"),
        DailyTip("If you can't find a house, check for the mailbox — addresses are usually on the mailbox.", "Navigation"),
        DailyTip("Take your lunch AWAY from your van. Mental break matters as much as physical break.", "Wellness"),
        DailyTip("Organize overflow at loadout — every minute sorting saves 3 minutes on route.", "Efficiency"),
        DailyTip("If a dog is out, don't risk it. Skip the stop, mark it, move on. Not worth a bite.", "Safety"),
        DailyTip("Your photo should clearly show the package AND the house number. Best DNR defense.", "Quality"),
        DailyTip("Call dispatch before you're 30 min behind. Earlier = easier to fix.", "Communication"),
        DailyTip("Keep a 'code book' (or use this app!) of gate/door codes. They repeat on routes.", "Productivity"),
        DailyTip("Stretch your back and neck during loadout. You'll thank yourself at stop 150.", "Wellness"),
        DailyTip("Dark outside? Slow down. The 2 extra minutes per stop is better than a missed step.", "Safety"),
        DailyTip("Smile at customers even when you're tired. Good interactions = no complaints.", "Professionalism"),
        DailyTip("Keep snacks in the cab, not the back. You won't want to dig for them later.", "Preparation"),
        DailyTip("Use Waze for nav, Flex app for scanning. Two devices is the pro setup.", "Tech"),
        DailyTip("If a gate code doesn't work, try #, 0, or 0000 before calling the customer.", "Problem Solving"),
        DailyTip("Your first week will feel slow. That's normal. Speed comes with route familiarity.", "Mindset"),
        DailyTip("Wear shoes with GOOD grip. Wet grass, slippery steps, gravel — you'll face it all.", "Safety"),
        DailyTip("Always lock the van, even if your phone is inside. Theft happens fast.", "Safety"),
        DailyTip("Ask other drivers about good bathroom spots on your route. Community knowledge!", "Tips"),
        DailyTip("When in doubt, call dispatch. They'd rather help than explain a failed route.", "Support"),
        DailyTip("Charge your backup battery every night. A dead phone = a dead route.", "Preparation"),
        DailyTip("Don't be afraid to use your flashlight during the day. Shaded porches need it too.", "Delivery"),
        DailyTip("Learn your DSP's preferred photo angles. Different stations have different standards.", "Quality"),
        DailyTip("Breathe. Take a moment. One package at a time. You got this.", "Mindset"),
        DailyTip("Write down your favorite route notes — they'll help when you get that route again.", "Productivity"),
        DailyTip("Keep a cheap power bank in your pocket for on-the-go charging.", "Tech"),
        DailyTip("Business deliveries before 3 PM. Residential can wait. Prioritize wisely.", "Strategy"),
        DailyTip("Watch your step getting out of the van every time. Ice, potholes, debris.", "Safety"),
        DailyTip("Ask your dispatcher for feedback after week 1. Show you want to improve.", "Career"),
        DailyTip("Help other drivers if you finish early. Good karma comes back around.", "Teamwork")
    )

    // ── Safety Items ──────────────────────────────────────────────
    val safetyItems = listOf(
        SafetyItem("s1", "Dog Encounter", "Stop, don't run, use package as shield", "DETAILED_DOG", Icons.Filled.Pets, SafetyPriority.CRITICAL),
        SafetyItem("s2", "Hostile Customer", "Return to van, lock doors, call dispatch", "Leave immediately. Do not engage. Call DSP and report.", Icons.Filled.PersonOff, SafetyPriority.CRITICAL),
        SafetyItem("s3", "Car Accident", "Call 911, then dispatch, take photos", "DETAILED_ACCIDENT", Icons.Filled.Build, SafetyPriority.CRITICAL),
        SafetyItem("s4", "Medical Emergency", "Call 911, tell dispatch, stay on line", "Call 911 first. Then call your DSP. Follow dispatcher instructions.", Icons.Filled.MedicalServices, SafetyPriority.CRITICAL),
        SafetyItem("s5", "Van Breakdown", "Hazards on, call dispatch, don't leave van", "DETAILED_BREAKDOWN", Icons.Filled.Build, SafetyPriority.HIGH),
        SafetyItem("s6", "Extreme Weather", "Pull over safely, wait it out, call dispatch", "DETAILED_WEATHER", Icons.Filled.Warning, SafetyPriority.HIGH),
        SafetyItem("s7", "Theft / Robbery", "Do not resist, give items, call 911 after", "Do NOT resist. Give them what they want. Your safety > packages. Call 911 immediately after.", Icons.Filled.MoodBad, SafetyPriority.CRITICAL),
        SafetyItem("s8", "Fire", "Evacuate area, call 911, notify dispatch", "Evacuate immediately. Call 911. Then call dispatch.", Icons.Filled.LocalFireDepartment, SafetyPriority.CRITICAL),
        SafetyItem("s9", "Gas Leak / Smell", "Turn off van, evacuate, call 911", "Turn engine off immediately. Evacuate area. Call 911 and dispatch.", Icons.Filled.Warning, SafetyPriority.CRITICAL),
        SafetyItem("s10", "Harassment", "Leave area, report to dispatch and police", "Leave the area immediately. Report to your DSP and local police if needed.", Icons.Filled.GppMaybe, SafetyPriority.HIGH)
    )

    // ── Loadout Checklist ─────────────────────────────────────────
    private val _checklistItems = MutableStateFlow(
        listOf(
            ChecklistItem("c1", "Van inspection complete (tires, lights, damage)", "Vehicle"),
            ChecklistItem("c2", "All totes/bags scanned", "Packages"),
            ChecklistItem("c3", "Overflow organized by driver aid number", "Packages"),
            ChecklistItem("c4", "Totes loaded in reverse stop order", "Packages"),
            ChecklistItem("c5", "Phone mount and navigation set up", "Tech"),
            ChecklistItem("c6", "Offline maps downloaded for route area", "Tech"),
            ChecklistItem("c7", "Backup battery pack charged and ready", "Tech"),
            ChecklistItem("c8", "Water, snacks, and lunch packed", "Personal"),
            ChecklistItem("c9", "Sunscreen and sunglasses applied", "Personal"),
            ChecklistItem("c10", "Comfortable shoes on, layers ready", "Personal"),
            ChecklistItem("c11", "Emergency contacts saved in phone", "Safety"),
            ChecklistItem("c12", "First 5 stops reviewed in Flex app", "Route"),
            ChecklistItem("c13", "Flashlight or headlamp in pocket", "Gear"),
            ChecklistItem("c14", "Sharpie and pen in cup holder", "Gear"),
            ChecklistItem("c15", "Charging cables for both phones", "Tech"),
            ChecklistItem("c16", "Hand sanitizer and wet wipes", "Personal"),
            ChecklistItem("c17", "Dispatch notified — ready to roll!", "Route")
        )
    )
    val checklistItems: StateFlow<List<ChecklistItem>> = _checklistItems.asStateFlow()
}
