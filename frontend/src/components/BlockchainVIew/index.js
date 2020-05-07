import React, { Component } from 'react';

import BASE_URL from '../../Constants';

import ListGroup from 'react-bootstrap/ListGroup';
import Table from 'react-bootstrap/Table';
import { Link } from 'react-router-dom';

import Pages from '../Pages';

import { withRouter } from 'react-router-dom';

import './Blockchain.css';

class BlockchainView extends Component {
    constructor(props) {
        super(props);
        this.state = {
            blocks: [],
            currentPage: 1
        }
    }

    handleClick(page) {
        this.setState({
            currentPage: Number(page)
        });
    }

    async componentWillMount() {
        await fetch('http://localhost:8080/' + 'block', {
            method: 'GET',
            headers: {
                'Accept': 'application/json',
                'Content-Type': 'application/json'
            }
        }).then(response => response.json()).then(blocks => {
            this.setState({ blocks : blocks.reverse() });
        });
    }

    render() {

        const { blocks, currentPage } = this.state;

        const indexLastBlock = currentPage * 30;
        const indexFirstBlock = indexLastBlock - 30;
        const currBlocks = blocks.slice(indexFirstBlock, indexLastBlock);

        const blockRows = currBlocks.map(block =>
            (
                <tr key={block.index}>
                    <td><Link className="" to={"block/" + block.hash}> {block.index} </Link></td>
                    <td><Link className="" to={"block/" + block.hash}> {block.hash.substring(0, 1) + "..." + block.hash.substring(20, 50) + '...'} </Link></td>
                    <td> {block.timestamp} </td>
                    <td> {block.difficultyTarget} </td>
                    <td> {block.transactions.length} </td>
                </tr>
            )
        )

        return (
            <div className="blockchain-table">
                <h5> Latest blocks </h5>

                <Pages currentPage={currentPage} perRow = {30} transactionsLength={blocks.length} handleClick={this.handleClick.bind(this)}></Pages>

                <Table responsive>
                    <thead>
                        <tr>
                            <th>Height</th>
                            <th>Hash</th>
                            <th>Timestamp</th>
                            <th>Difficulty</th>
                            <th>Transaction nr</th>
                        </tr>
                    </thead>
                    <tbody>
                        {blockRows}
                    </tbody>
                </Table>
            </div>
        );
    }
}

export default withRouter(BlockchainView);