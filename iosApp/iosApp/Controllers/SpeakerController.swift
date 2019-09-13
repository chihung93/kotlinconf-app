import Foundation
import UIKit
import KotlinConfAPI
import Nuke

class SpeakerController : UIViewController {
    @IBOutlet weak var nameLabel: UILabel!
    @IBOutlet weak var photoView: UIImageView!
    @IBOutlet weak var positionLabel: UILabel!
    @IBOutlet weak var descriptionLabel: UILabel!
    @IBOutlet weak var talksContainer: UIStackView!

    var speaker: SpeakerData!

    override func viewDidLoad() {
        nameLabel.text = speaker.fullName.uppercased()

        if let profilePicture = speaker.profilePicture {
            Nuke.loadImage(with: URL(string: profilePicture)!, into: photoView)
        }

        descriptionLabel.attributedText = TextWithLineHeight(text: speaker.bio, height: 24)
        positionLabel.text = speaker.tagLine
    }

    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)

        let sessions = Conference.speakerSessions(speakerId: speaker.id)

        for card in sessions {
            let view = SessionCardView()
            view.card = card
            setupCard(view)
            talksContainer.addArrangedSubview(view)
            talksContainer.setCustomSpacing(5.0, after: view)
        }
    }

    private func setupCard(_ view: SessionCardView) {
        view.onTouch = {
            let board = UIStoryboard(name: "Main", bundle: nil)
            let controller = board.instantiateViewController(withIdentifier: "Session") as! SessionController
            controller.card = view.card
            self.navigationController?.pushViewController(controller, animated: true)
        }
    }

    override func viewWillDisappear(_ animated: Bool) {
        super.viewWillDisappear(animated)
        for item in talksContainer.subviews {
            let cardView = item as! SessionCardView
            cardView.cleanup()
            talksContainer.removeArrangedSubview(item)
        }
    }

    @IBAction func backButtonTouch(_ sender: Any) {
        self.navigationController?.popViewController(animated: true)
    }
}
